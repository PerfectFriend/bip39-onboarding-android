package com.example.bip39onboarding.security

interface SeedStorage {
    fun isSetup(): Boolean
    fun save(
        payload: CryptoUtils.EncryptedPayload,
        pinHash: String,
        panicPinHash: String,
        wordCount: Int,
        fingerprint: String,
        hasPassphrase: Boolean = false
    )
    fun getPayload(): CryptoUtils.EncryptedPayload?
    fun getPinHash(): String?
    fun getPanicPinHash(): String?
    fun getFingerprint(): String?
    fun getWordCount(): Int
    fun hasPassphrase(): Boolean = false
    fun wipe()
}

class EspSeedStorageAdapter(private val inner: SecureSeedStorage) : SeedStorage {
    override fun isSetup() = inner.isSetup()
    override fun save(
        payload: CryptoUtils.EncryptedPayload,
        pinHash: String,
        panicPinHash: String,
        wordCount: Int,
        fingerprint: String,
        hasPassphrase: Boolean
    ) = inner.save(payload, pinHash, panicPinHash, wordCount, fingerprint)
    override fun getPayload() = inner.getPayload()
    override fun getPinHash() = inner.getPinHash()
    override fun getPanicPinHash() = inner.getPanicPinHash()
    override fun getFingerprint() = inner.getFingerprint()
    override fun getWordCount() = inner.getWordCount()
    override fun hasPassphrase() = false
    override fun wipe() = inner.wipe()
}

class TinkSeedStorageAdapter(private val inner: TinkSeedStorage) : SeedStorage {
    override fun isSetup() = inner.isSetup()
    override fun save(
        payload: CryptoUtils.EncryptedPayload,
        pinHash: String,
        panicPinHash: String,
        wordCount: Int,
        fingerprint: String,
        hasPassphrase: Boolean
    ) = inner.save(payload, pinHash, panicPinHash, wordCount, fingerprint, hasPassphrase)
    override fun getPayload() = inner.getPayload()
    override fun getPinHash() = inner.getPinHash()
    override fun getPanicPinHash() = inner.getPanicPinHash()
    override fun getFingerprint() = inner.getFingerprint()
    override fun getWordCount() = inner.getWordCount()
    override fun hasPassphrase() = inner.hasPassphrase()
    override fun wipe() = inner.wipe()
}
