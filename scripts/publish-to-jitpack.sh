#!/usr/bin/env bash
# ZERO-TOUCH publish → JitPack for PerfectFriend/bip39-onboarding-android ONLY
set -euo pipefail

readonly OWNER="PerfectFriend"
readonly REPO="bip39-onboarding-android"
readonly WEB="https://github.com/${OWNER}/${REPO}"
readonly TAG="1.2.2"
readonly GRADLE_VER="8.9"

WORKDIR="$(mktemp -d -t bip39-jitpack-XXXXXX)"
cleanup() { rm -rf "${WORKDIR}"; }
trap cleanup EXIT

log()  { printf '\n\033[1;36m==>\033[0m %s\n' "$*"; }
ok()   { printf '\033[1;32m✓\033[0m %s\n' "$*"; }
die()  { printf '\033[1;31m✗ %s\033[0m\n' "$*" >&2; exit 1; }

resolve_token() {
  if [[ -n "${GH_TOKEN:-}" ]]; then echo "${GH_TOKEN}"; return; fi
  if [[ -n "${GITHUB_TOKEN:-}" ]]; then echo "${GITHUB_TOKEN}"; return; fi
  if command -v gh >/dev/null 2>&1 && gh auth token >/dev/null 2>&1; then
    gh auth token; return
  fi
  echo ""
}

TOKEN="$(resolve_token)"
[[ -n "${TOKEN}" ]] || die "Нет GitHub token. Один раз: gh auth login  ИЛИ  export GH_TOKEN=ghp_xxx  — потом снова запусти этот скрипт."
ok "Token OK"

AUTH_CLONE="https://x-access-token:${TOKEN}@github.com/${OWNER}/${REPO}.git"

log "Clone ONLY ${WEB}"
git clone --depth 50 "${AUTH_CLONE}" "${WORKDIR}/repo"
cd "${WORKDIR}/repo"
git checkout main 2>/dev/null || git checkout master
git branch -M main
case "$(git remote get-url origin)" in
  *"${OWNER}/${REPO}"*) ok "origin=${OWNER}/${REPO}" ;;
  *) die "Wrong remote" ;;
esac

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENGINE=""
for c in \
  "${SCRIPT_DIR}/.." \
  "${SCRIPT_DIR}" \
  "${SCRIPT_DIR}/bip39-onboarding-android" \
  "${SCRIPT_DIR}/../bip39-onboarding-android" \
  "/home/workdir/artifacts/bip39-onboarding-android"
do
  if [[ -d "${c}/library/src" && -f "${c}/settings.gradle.kts" ]]; then
    ENGINE="$(cd "$c" && pwd)"
    break
  fi
done

if [[ -n "${ENGINE}" ]]; then
  log "Sync engine from ${ENGINE}"
  if command -v rsync >/dev/null 2>&1; then
    rsync -a --exclude '.git' --exclude 'build' --exclude '.gradle' --exclude 'local.properties' "${ENGINE}/" "${WORKDIR}/repo/"
  else
    (cd "${ENGINE}" && tar cf - --exclude .git --exclude build --exclude .gradle .) | (cd "${WORKDIR}/repo" && tar xf -)
  fi
  ok "Synced"
else
  printf '\033[1;33m!\033[0m No local engine — only fixing publish files on remote tree\n'
fi

cd "${WORKDIR}/repo"
[[ -d .git ]] || die ".git missing"

cat > settings.gradle.kts << 'EOF'
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "bip39-onboarding-android"
include(":library")
include(":sample")
EOF

cat > build.gradle.kts << 'EOF'
plugins {
    id("com.android.library") version "8.7.2" apply false
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
EOF

cat > gradle.properties << 'EOF'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
EOF

log "Gradle wrapper"
mkdir -p gradle/wrapper
curl -fsSL -o gradlew "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VER}.0/gradlew"
chmod +x gradlew
curl -fsSL -o gradle/wrapper/gradle-wrapper.jar \
  "https://github.com/gradle/gradle/raw/v${GRADLE_VER}.0/gradle/wrapper/gradle-wrapper.jar"
cat > gradle/wrapper/gradle-wrapper.properties << EOF
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-${GRADLE_VER}-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

cat > jitpack.yml << 'EOF'
jdk:
  - openjdk17
before_install:
  - |
    set -e
    chmod +x gradlew 2>/dev/null || true
    mkdir -p gradle/wrapper
    if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
      curl -fsSL -o gradle/wrapper/gradle-wrapper.jar \
        https://github.com/gradle/gradle/raw/v8.9.0/gradle/wrapper/gradle-wrapper.jar
    fi
    if [ ! -f gradlew ]; then
      curl -fsSL -o gradlew https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradlew
      chmod +x gradlew
    fi
install:
  - ./gradlew :library:publishToMavenLocal -x test --no-daemon --stacktrace
EOF

[[ -f library/build.gradle.kts ]] || die "library/build.gradle.kts missing — положите engine ZIP рядом со скриптом"
sed -i.bak -E "s/version = \"[0-9]+\\.[0-9]+\\.[0-9]+\"/version = \"${TAG}\"/" library/build.gradle.kts 2>/dev/null || true
rm -f library/build.gradle.kts.bak

if [[ ! -f sample/build.gradle.kts ]]; then
  mkdir -p sample/src/main
  cat > sample/build.gradle.kts << 'EOF'
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.example.sample"
    compileSdk = 35
    defaultConfig { applicationId = "com.example.bip39sample"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0" }
    buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(project(":library"))
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
}
EOF
  echo '<?xml version="1.0"?><manifest xmlns:android="http://schemas.android.com/apk/res/android"><application android:label="sample"/></manifest>' > sample/src/main/AndroidManifest.xml
fi

log "Push to ${OWNER}/${REPO}"
git config user.email "jitpack-bot@users.noreply.github.com"
git config user.name "JitPack Publish Bot"
git remote set-url origin "${AUTH_CLONE}"
git add -A
git add -f gradlew gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.properties || true
if ! git diff --cached --quiet; then
  git commit -m "release: ${TAG} zero-touch JitPack (${OWNER}/${REPO})"
fi
git push -u origin main
git tag -d "${TAG}" 2>/dev/null || true
git tag -a "${TAG}" -m "Release ${TAG}"
git push origin "refs/tags/${TAG}" --force
ok "Pushed main + tag ${TAG}"

log "Trigger JitPack"
curl -fsSL "https://jitpack.io/com/github/${OWNER}/${REPO}/${TAG}/build.log" -o /tmp/jitpack-build.log 2>/dev/null || true
sleep 3
tail -30 /tmp/jitpack-build.log 2>/dev/null || true

echo
echo "============================================================================="
echo "  ${WEB}"
echo "  tag ${TAG}"
echo "  log: https://jitpack.io/com/github/${OWNER}/${REPO}/${TAG}/build.log"
echo "  implementation(\"com.github.${OWNER}.${REPO}:library:${TAG}\")"
echo "============================================================================="
