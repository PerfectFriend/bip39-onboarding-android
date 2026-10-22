#!/bin/sh
APP_HOME=$(cd "${0%/*}" && pwd -P) || exit
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ -n "$JAVA_HOME" ]; then JAVACMD=$JAVA_HOME/bin/java; else JAVACMD=java; fi
if [ ! -f "$CLASSPATH" ]; then
  mkdir -p "$APP_HOME/gradle/wrapper"
  curl -fsSL -o "$CLASSPATH" https://github.com/gradle/gradle/raw/v8.9.0/gradle/wrapper/gradle-wrapper.jar
fi
exec "$JAVACMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
