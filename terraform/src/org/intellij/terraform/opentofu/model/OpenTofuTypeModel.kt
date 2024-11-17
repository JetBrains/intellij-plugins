// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.model

import com.intellij.openapi.fileTypes.FileType
import org.intellij.terraform.config.model.*
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_ENCRYPTION
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_ENCRYPTION_METHOD_BLOCK
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_KEY_PROVIDER
import org.intellij.terraform.opentofu.OpenTofuFileType


//<editor-fold desc="Encryption providers">
internal class AbstractEncryptionProvider(
  override val type: String,
  properties: List<PropertyOrBlockType>,
  blockType: BlockType? = null,
) : BlockType(literal = TOFU_KEY_PROVIDER,
              args = 2,
              description = blockType?.description,
              description_kind = blockType?.description_kind,
              optional = blockType?.optional == true,
              required = blockType?.required == true,
              computed = blockType?.computed == true,
              deprecated = blockType?.deprecated,
              conflictsWith = blockType?.conflictsWith,
              nesting = blockType?.nesting,
              properties = withDefaults(properties, emptyMap())), NamedType {
  override fun toString(): String {
    return "Encryption Provider (type='$type')"
  }

  override val presentableText: String
    get() = "$literal ($type)"
}

private val abstractProviderInstance: BlockType = BlockType(literal = TOFU_KEY_PROVIDER)

internal val Pbkdf2Provider = AbstractEncryptionProvider("pbkdf2", listOf(
  PropertyType("passphrase", Types.String, required = true, description = HCLBundle.message("opentofu.key_provider.pbkdf2.passphrase.description")),
  PropertyType("key_length", Types.Number, description = HCLBundle.message("opentofu.key_provider.pbkdf2.key_length.description", 1, 32),
               hint = SimpleValueHint(*(1..32).reversed().map { it.toString() }.toTypedArray())),
  PropertyType("iterations", Types.Number, description = HCLBundle.message("opentofu.key_provider.pbkdf2.iterations.description")),
  PropertyType("salt_length", Types.Number, description = HCLBundle.message("opentofu.key_provider.pbkdf2.salt_length.description")),
  PropertyType("hash_function", Types.String, hint = SimpleValueHint("sha256", "sha512"), description = HCLBundle.message("opentofu.key_provider.pbkdf2.hash_function.description"))
), abstractProviderInstance)

internal val AwsKmsProvider = AbstractEncryptionProvider("aws_kms", listOf(
  PropertyType("kms_key_id", Types.String, required = true, description = HCLBundle.message("opentofu.key_provider.aws_kms.kms_key_id.description")),
  PropertyType("key_spec", Types.String, required = true, hint = SimpleValueHint("SYMMETRIC_DEFAULT",
                                                                                 "HMAC_224", "HMAC_256", "HMAC_384", "HMAC_512",
                                                                                 "RSA_2048", "RSA_3072", "RSA_4096",
                                                                                 "ECC_NIST_P256", "ECC_NIST_P384", "ECC_NIST_P521", "ECC_SECG_P256K1",
                                                                                 "SM2"), description = HCLBundle.message("opentofu.key_provider.aws_kms.key_spec.description")),
  PropertyType("region", Types.String, required = true)
), abstractProviderInstance)

internal val GcpKmsProvider = AbstractEncryptionProvider("gcp_kms", listOf(
  PropertyType("kms_encryption_key", Types.String, required = true, description = HCLBundle.message("opentofu.key_provider.gcp_kms.kms_encryption_key.description")),
  PropertyType("key_length", Types.Number, required = true, description = HCLBundle.message("opentofu.key_provider.gcp_kms.key_length.description", 1, 1024)),
), abstractProviderInstance)

internal val encryptionKeyProviders = listOf(AwsKmsProvider, GcpKmsProvider, Pbkdf2Provider).associateBy { it.type }
//</editor-fold>

//<editor-fold desc="Encryption Methods">
internal class AbstractEncryptionMethod(
  override val type: String,
  properties: List<PropertyOrBlockType>,
  blockType: BlockType? = null,
) : BlockType(literal = TOFU_ENCRYPTION_METHOD_BLOCK,
              args = 2,
              description = blockType?.description,
              description_kind = blockType?.description_kind,
              optional = blockType?.optional == true,
              required = blockType?.required == true,
              computed = blockType?.computed == true,
              deprecated = blockType?.deprecated,
              conflictsWith = blockType?.conflictsWith,
              nesting = blockType?.nesting,
              properties = withDefaults(properties, emptyMap())), NamedType {
  override fun toString(): String {
    return "Encryption Method (type='$type')"
  }

  override val presentableText: String
    get() = "$literal ($type)"
}

internal val AbstractEncryptionMethodInstance = BlockType(TOFU_ENCRYPTION_METHOD_BLOCK, args = 2)

internal val UnencryptedMethod = AbstractEncryptionMethod("unencrypted", emptyList(), AbstractEncryptionMethodInstance)

internal val AesGcmMethod = AbstractEncryptionMethod("aes_gcm",
                                                     listOf(
                                                       PropertyType("keys", Types.String, hint = ReferenceHint("key_provider.#name"), required = true),
                                                     ),
                                                     AbstractEncryptionMethodInstance)

internal val encryptionMethods = listOf(AesGcmMethod, UnencryptedMethod).associateBy { it.type }
//</editor-fold>

//<editor-fold desc="Plan and state encryption">
val fallbackMethod = BlockType("fallback", properties = listOf(
  PropertyType("method", Types.String, hint = ReferenceHint("method.#name"))).toMap())

internal val State: BlockType = BlockType("state", properties = listOf(
  PropertyType("method", Types.String, hint = ReferenceHint("method.#name")),
  PropertyType("enforced", Types.Boolean),
  fallbackMethod
).toMap())

internal val Plan: BlockType = BlockType("plan", properties = listOf(
  PropertyType("method", Types.String, hint = ReferenceHint("method.#name")),
  PropertyType("enforced", Types.Boolean),
  fallbackMethod
).toMap())
//</editor-fold>

internal val RemoteStateDataSource: BlockType = BlockType("remote_state_data_source", 1, properties = listOf(
  PropertyType(name = "method", type = Types.String)
).toMap())

internal val DefaultDataSource: BlockType = BlockType("default", properties = listOf(
  PropertyType(name = "method", type = Types.String)
).toMap())

internal val RemoteStateDataSources: BlockType = BlockType("remote_state_data_sources", properties = listOf(
  DefaultDataSource,
  RemoteStateDataSource
).toMap())

internal class EncryptionBlockType : BlockType(TOFU_ENCRYPTION, optional = true, properties = listOf(
  BlockType(TOFU_KEY_PROVIDER, args = 2),
  AbstractEncryptionMethodInstance,
  State,
  Plan,
  RemoteStateDataSources
).toMap()) {
  override fun canBeUsedIn(fileType: FileType): Boolean {
    return fileType == OpenTofuFileType
  }
}

internal fun getEncryptionKeyProviderProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
  val type = block.getNameElementUnquoted(1) ?: return emptyMap()
  return encryptionKeyProviders[type]?.properties ?: emptyMap()
}

internal fun getEncryptionMethodProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
  val type = block.getNameElementUnquoted(1) ?: return emptyMap()
  return encryptionMethods[type]?.properties ?: emptyMap()
}
