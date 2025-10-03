// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.util.concurrency.annotations.RequiresReadLock
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.Constants.HCL_ASSERT_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_ATLAS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_BACKEND_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_CHECK_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_CLOUD_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_CONNECTION_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_COUNT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DEFAULT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DYNAMIC_BLOCK_CONTENT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DYNAMIC_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_EPHEMERAL_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_IMPORT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_LIFECYCLE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_LOCALS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MOVED_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_OUTPUT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_POSTCONDITION_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PRECONDITION_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVISIONER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_REMOVED_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_REQUIRED_PROVIDERS
import org.intellij.terraform.config.Constants.HCL_VALIDATION_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VARIABLE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_WORKSPACES_BLOCK_IDENTIFIER
import org.intellij.terraform.config.model.local.LocalProviderNamesService
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.isTerraformCompatiblePsiFile
import org.intellij.terraform.opentofu.model.EncryptionBlockType

enum class ProviderTier(val label: String) {
  TIER_BUILTIN("builtin"),
  TIER_LOCAL("local"),
  TIER_OFFICIAL("official"),
  TIER_PARTNER("partner"),
  TIER_COMMUNITY("community"),
  TIER_NONE("none");

  companion object {
    fun findByLabel(label: String): ProviderTier? {
      return entries.find { it.label == label }
    }

    val PreferedProviders: Set<ProviderTier> = setOf(TIER_BUILTIN, TIER_LOCAL, TIER_OFFICIAL)
  }
}

class TfTypeModel(
  resources: List<ResourceType> = emptyList(),
  dataSources: List<DataSourceType> = emptyList(),
  providers: List<ProviderType> = emptyList(),
  provisioners: List<ProvisionerType> = emptyList(),
  backends: List<BackendType> = emptyList(),
  functions: List<TfFunction> = emptyList(),
  providerDefinedFunctions: List<TfFunction> = emptyList(),
  ephemeralResources: List<EphemeralType> = emptyList(),
) {

  val provisioners: List<ProvisionerType> = provisioners.sortedBy { it.type }
  val backends: List<BackendType> = backends.sortedBy { it.type }
  val functions: List<TfFunction> = functions.sortedBy { it.name }
  val providerDefinedFunctions: List<TfFunction> = providerDefinedFunctions.sortedBy { it.name }

  val providersByFullName: Map<String, ProviderType> = providers
    .groupBy { it.fullName.lowercase() }
    .mapValues { (_, values) -> values.minBy { it.tier } }

  val resourcesByProvider: Map<String, List<ResourceType>>
  val dataSourcesByProvider: Map<String, List<DataSourceType>>
  val ephemeralByProvider: Map<String, List<EphemeralType>>

  init {
    val loadedProviders = providersByFullName.values.toSet()

    resourcesByProvider = resources.filter { it.provider in loadedProviders }.groupBy { it.provider.fullName.lowercase() }
    dataSourcesByProvider = dataSources.filter { it.provider in loadedProviders }.groupBy { it.provider.fullName.lowercase() }
    ephemeralByProvider = ephemeralResources.filter { it.provider in loadedProviders }.groupBy { it.provider.fullName.lowercase() }
  }

  companion object {

    private val VersionProperty = PropertyType("version", Types.String, hint = SimpleValueHint("VersionRange"), injectionAllowed = false)
    val TerraformRequiredVersion: PropertyType = PropertyType("required_version", Types.String, hint = SimpleValueHint("VersionRange"),
                                                              injectionAllowed = false)

    private val DependsOnProperty: PropertyType = PropertyType(
      "depends_on",
      Types.Array,
      ReferenceHint("resource.#name", "data_source.#name", "module.#name", "variable.#name", "ephemeral.#name")
    )
    private val CountProperty = PropertyType(HCL_COUNT_IDENTIFIER, Types.Number, conflictsWith = listOf("for_each"))
    private val ForEachProperty = PropertyType("for_each", Types.Any, conflictsWith = listOf(HCL_COUNT_IDENTIFIER))
    private val ProviderProperty = PropertyType("provider", Types.String, hint = ReferenceHint("provider.#type", "provider.#alias"))

    val DescriptionProperty: PropertyType = PropertyType("description", Types.String)
    val SensitiveProperty: PropertyType = PropertyType("sensitive", Types.Boolean)
    val NullableProperty: PropertyType = PropertyType("nullable", Types.Boolean)

    val Atlas: BlockType = BlockType(HCL_ATLAS_IDENTIFIER, 0, properties = listOf(
      PropertyType("name", Types.String, injectionAllowed = false, required = true)).toMap())
    val Module: BlockType = BlockType(HCL_MODULE_IDENTIFIER, 1, properties = listOf(
      PropertyType(HCL_SOURCE_IDENTIFIER, Types.String, hint = SimpleValueHint("Url"), required = true),
      VersionProperty,
      DependsOnProperty,
      CountProperty,
      ForEachProperty,
      PropertyType("providers", MapType(Types.String))
    ).toMap())

    val ErrorMessageProperty: PropertyType = PropertyType("error_message", Types.String)
    val ConditionProperty: PropertyType = PropertyType("condition", Types.Boolean, injectionAllowed = false)
    val VariableType: PropertyType = PropertyType("type", Types.Any, injectionAllowed = false)
    val VariableDefault: PropertyType = PropertyType(HCL_DEFAULT_IDENTIFIER, Types.Any)
    val VariableValidation: BlockType = BlockType(HCL_VALIDATION_IDENTIFIER, 0, properties = listOf(
      ConditionProperty,
      ErrorMessageProperty
    ).toMap())
    val EphemeralProperty: PropertyType = PropertyType(HCL_EPHEMERAL_IDENTIFIER, Types.Boolean)
    val Variable: BlockType = BlockType(HCL_VARIABLE_IDENTIFIER, 1, properties = listOf<PropertyOrBlockType>(
      VariableType,
      VariableDefault,
      VariableValidation,
      DescriptionProperty,
      SensitiveProperty,
      NullableProperty,
      EphemeralProperty
    ).toMap())

    val Connection: BlockType = BlockType(HCL_CONNECTION_IDENTIFIER, 0, properties = listOf(
      PropertyType("type", Types.String,
                   description = "The connection type that should be used. Valid types are \"ssh\" and \"winrm\" This defaults to \"ssh\"."),
      PropertyType("user", Types.String),
      PropertyType("password", Types.String),
      PropertyType("host", Types.String),
      PropertyType("port", Types.Number),
      PropertyType("timeout", Types.String),
      PropertyType("script_path", Types.String)
    ).toMap())
    val ConnectionPropertiesSSH: Map<String, PropertyOrBlockType> = listOf(
      // ssh
      PropertyType("key_file", Types.String, deprecated = "Use 'private_key'"),
      PropertyType("private_key", Types.String),
      PropertyType("agent", Types.Boolean),

      // bastion ssh
      PropertyType("bastion_host", Types.String),
      PropertyType("bastion_port", Types.Number),
      PropertyType("bastion_user", Types.String),
      PropertyType("bastion_password", Types.String),
      PropertyType("bastion_private_key", Types.String),
      PropertyType("bastion_key_file", Types.String, deprecated = "Use 'bastion_private_key'")
    ).toMap()
    val ConnectionPropertiesWinRM: Map<String, PropertyOrBlockType> = listOf(
      // winrm
      PropertyType("https", Types.Boolean),
      PropertyType("insecure", Types.Boolean),
      PropertyType("cacert", Types.String)
    ).toMap()

    val PreconditionBlock: BlockType = BlockType(HCL_PRECONDITION_BLOCK_IDENTIFIER, 0, properties = listOf(ConditionProperty, ErrorMessageProperty).toMap())
    val PostconditionBlock: BlockType = BlockType(HCL_POSTCONDITION_BLOCK_IDENTIFIER, 0, properties = listOf(ConditionProperty, ErrorMessageProperty).toMap())

    val ValueProperty: PropertyType = PropertyType("value", Types.Any, required = true)
    val Output: BlockType = BlockType(HCL_OUTPUT_IDENTIFIER, 1, properties = listOf(
      ValueProperty,
      DescriptionProperty,
      DependsOnProperty,
      SensitiveProperty,
      PreconditionBlock,
      EphemeralProperty
    ).toMap())

    val ResourceLifecycle: BlockType = BlockType(HCL_LIFECYCLE_IDENTIFIER, 0,
                                                 description = "Describe to Terraform how to connect to the resource for provisioning", // TODO: Improve description
                                                 properties = listOf(
                                                   PropertyType("create_before_destroy", Types.Boolean),
                                                   PropertyType("prevent_destroy", Types.Boolean),
                                                   PropertyType("ignore_changes", ListType(Types.Any)),
                                                   PropertyType("replace_triggered_by", ListType(Types.Any)),
                                                   PreconditionBlock,
                                                   PostconditionBlock
                                                 ).toMap())
    val AbstractResourceProvisioner: BlockType = BlockType(HCL_PROVISIONER_IDENTIFIER, 1, properties = listOf(
      Connection
    ).toMap())

    val AbstractResourceDynamicContent: BlockType = BlockType(HCL_DYNAMIC_BLOCK_CONTENT_IDENTIFIER, 0, required = true)
    val ResourceDynamic: BlockType = BlockType(HCL_DYNAMIC_BLOCK_IDENTIFIER, 1, properties = listOf<PropertyOrBlockType>(
      PropertyType("for_each", Types.Any, required = true),
      PropertyType("labels", Types.Array),
      PropertyType("iterator", Types.Identifier),
      AbstractResourceDynamicContent
    ).toMap())

    @JvmField
    val AbstractResource: BlockType = BlockType(HCL_RESOURCE_IDENTIFIER, 2, properties = listOf<PropertyOrBlockType>(
      PropertyType("id", Types.String, injectionAllowed = false, description = "A unique ID for this resource", optional = false,
                   required = false, computed = true),
      CountProperty,
      ForEachProperty,
      DependsOnProperty,
      ProviderProperty,
      ResourceLifecycle,
      ResourceDynamic,
      // Also may have connection? and provisioner+ blocks
      Connection,
      AbstractResourceProvisioner
    ).toMap())

    val AbstractEphemeralResource: BlockType = BlockType(HCL_EPHEMERAL_IDENTIFIER, 2, properties = listOf<PropertyOrBlockType>(
      DependsOnProperty,
      CountProperty,
      ForEachProperty,
      ProviderProperty,
      ResourceLifecycle
    ).toMap())

    @JvmField
    val AbstractDataSource: BlockType = BlockType(HCL_DATASOURCE_IDENTIFIER, 2, properties = listOf(
      PropertyType("id", Types.String, injectionAllowed = false, description = "A unique ID for this data source", optional = false,
                   required = false, computed = true),
      CountProperty,
      ForEachProperty,
      DependsOnProperty,
      ResourceLifecycle,
      ProviderProperty,
    ).toMap())

    @JvmField
    val AbstractProvider: BlockType = BlockType(HCL_PROVIDER_IDENTIFIER, 1, required = false, properties = listOf(
      PropertyType("alias", Types.String, injectionAllowed = false),
      VersionProperty
    ).toMap())
    val AbstractBackend: BlockType = BlockType(HCL_BACKEND_IDENTIFIER, 1)
    val FromProperty: PropertyType = PropertyType("from", Types.Identifier, required = true)
    val ToProperty: PropertyType = PropertyType("to", Types.Identifier, required = true)
    val Moved: BlockType = BlockType(HCL_MOVED_BLOCK_IDENTIFIER, properties = listOf(FromProperty, ToProperty).toMap())
    val Cloud: BlockType = BlockType(HCL_CLOUD_IDENTIFIER, properties = listOf(
      PropertyType("organization", Types.String),
      BlockType(HCL_WORKSPACES_BLOCK_IDENTIFIER, required = true, properties = listOf(
        PropertyType("name", Types.String, conflictsWith = listOf("tags")),
        PropertyType("tags", ListType(Types.String), conflictsWith = listOf("name"))
      ).toMap())
    ).toMap())
    val Terraform: BlockType = BlockType(HCL_TERRAFORM_IDENTIFIER, properties = listOf<PropertyOrBlockType>(
      TerraformRequiredVersion,
      BlockType(HCL_TERRAFORM_REQUIRED_PROVIDERS),
      BlockType("provider_meta", args = 1),
      EncryptionBlockType(),
      Cloud,
      AbstractBackend,
      PropertyType("experiments", Types.Array),
    ).toMap())
    val Locals: BlockType = BlockType(HCL_LOCALS_IDENTIFIER)
    val Import: BlockType = BlockType(HCL_IMPORT_IDENTIFIER, properties = listOf(
      ToProperty,
      PropertyType("id", Types.String, required = true, conflictsWith = listOf("identity")),
      BlockType("identity", args = 0, conflictsWith = listOf("id")),
      PropertyType("provider", Types.String, required = false),
      ForEachProperty
    ).toMap())

    val AssertBlock: BlockType = BlockType(HCL_ASSERT_BLOCK_IDENTIFIER, 0, required = true,
                                           properties = listOf(ConditionProperty, ErrorMessageProperty).toMap())

    val CheckBlock: BlockType = BlockType(HCL_CHECK_BLOCK_IDENTIFIER, 1, properties = listOf(AbstractDataSource, AssertBlock).toMap())

    val RemovedBlock: BlockType = BlockType(HCL_REMOVED_BLOCK_IDENTIFIER, 0, properties = listOf(FromProperty, ResourceLifecycle).toMap())

    val RootBlocks: List<BlockType> = listOf(
      AbstractDataSource,
      AbstractEphemeralResource,
      AbstractProvider,
      AbstractResource,
      Atlas,
      CheckBlock,
      Import,
      Locals,
      Module,
      Moved,
      Output,
      RemovedBlock,
      Terraform,
      Variable
    )

    val RootBlocksMap: Map<String, BlockType> = RootBlocks.associateBy(BlockType::literal)

    fun getResourcePrefix(identifier: String): String {
      val stringList = identifier.split("_", limit = 2)
      val prefix = if (stringList.size < 2) identifier else stringList[0]
      return prefix
    }

    fun getResourceName(identifier: String): String {
      val stringList = identifier.split("_", limit = 2)
      val id = if (stringList.size < 2) identifier else stringList[1]
      return id
    }

    @RequiresReadLock
    fun collectProviderLocalNames(psiElement: PsiElement): Map<String, String> {
      val providerNamesService = LocalProviderNamesService.getInstance()
      val gists = getContainingDir(psiElement)?.childrenOfType<PsiFile>()
                    ?.filter { file -> isTerraformCompatiblePsiFile(file) }
                    ?.map { providerNamesService.providersNamesGist.getFileData(it) } ?: return emptyMap<String, String>()
      return gists.flatMap { it.entries }.associate { it.key to it.value }
    }

    private fun getContainingDir(psiElement: PsiElement?): PsiDirectory? {
      val containingDir = psiElement?.let { getContainingFile(it)?.parent } ?: return null
      return if (containingDir.isDirectory) containingDir else null
    }

    @RequiresReadLock
    fun getTerraformBlock(psiFile: PsiFile?): HCLBlock? {
      val terraformRootBlock = psiFile?.childrenOfType<HCLBlock>()?.firstOrNull { TfPsiPatterns.TerraformRootBlock.accepts(it) }
      return terraformRootBlock
    }
  }

  private fun <T> List<T>.findBinary(elt: String, k: (T) -> String): T? =
    findIndexBinary(elt, k).takeIf { it != -1 }?.let { this[it] }

  private fun <T> List<T>.findIndexBinary(elt: String, k: (T) -> String): Int {
    var low = 0
    var high: Int = this.size - 1

    while (low <= high) {
      val mid = (low + high) ushr 1
      val midVal: T = this[mid]
      val cmp: Int = k(midVal).compareTo(elt)
      when {
        cmp < 0 -> low = mid + 1
        cmp > 0 -> high = mid - 1
        else -> return mid
      }
    }
    return -1
  }

  private fun getProviderNameForIdentifier(identifier: String, psiElement: PsiElement? = null): String {
    val localNames = psiElement?.let { collectProviderLocalNames(it) } ?: emptyMap()
    val providerShortName = isProviderTypeDefined(psiElement) ?: getResourcePrefix(identifier)
    val providerFullName = localNames[providerShortName]
                           ?: Constants.OfficialProvidersNamespace.map { "$it/$providerShortName" }.firstOrNull { providersByFullName.containsKey(it) }
                           ?: "hashicorp/$providerShortName" //The last resort
    return providerFullName.lowercase()
  }

  private fun isProviderTypeDefined(psiElement: PsiElement?): String? {
    if (psiElement == null || !TfPsiPatterns.ProviderDefinedHclBlocks.accepts(psiElement))
      return null

    val hclObject = psiElement.childrenOfType<HCLObject>().firstOrNull() ?: return null
    val providerProperty = hclObject.findProperty(HCL_PROVIDER_IDENTIFIER)?.value?.text ?: return null
    return providerProperty.trim()
      .split('.')
      .firstOrNull()
      ?.takeIf { it.isNotEmpty() }
  }

  fun getResourceType(name: String, psiElement: PsiElement? = null): ResourceType? =
    lookupType(name, psiElement, resourcesByProvider, allResources())

  fun getDataSourceType(name: String, psiElement: PsiElement? = null): DataSourceType? =
    lookupType(name, psiElement, dataSourcesByProvider, allDataSources())

  fun getEphemeralType(name: String, psiElement: PsiElement? = null): EphemeralType? =
    lookupType(name, psiElement, ephemeralByProvider, allEphemeralResources())

  private fun <T : ResourceOrDataSourceType> lookupType(
    name: String,
    psiElement: PsiElement?,
    typesMap: Map<String, List<T>>,
    allTypes: Sequence<T>,
  ): T? {
    val providerName = getProviderNameForIdentifier(name, psiElement)

    // Try to find the type in the provider-specific map first
    val typesCollection = typesMap[providerName] ?: return null

    // Fallback: if not found, search across all types
    // In most cases (approx. 95%), the type will be found via the provider-specific map,
    // so the fallback search should have minimal performance impact.
    return typesCollection.firstOrNull { it.type == name } ?: allTypes.find { it.type == name }
  }

  fun getProviderType(name: String, psiElement: PsiElement? = null): ProviderType? {
    val providerName = getProviderNameForIdentifier(name, psiElement)
    return providersByFullName[providerName]
  }

  fun getProvisionerType(name: String): ProvisionerType? {
    return provisioners.findBinary(name) { it.type }
  }

  fun getBackendType(name: String): BackendType? {
    return backends.findBinary(name) { it.type }
  }

  fun getFunction(name: String): TfFunction? {
    return functions.findBinary(name) { it.name }
  }

  fun getProviderFunction(provider: String, functionName: String): TfFunction? {
    return providerDefinedFunctions.find { it.name == functionName && it.providerType == provider }
  }

  fun getByFQN(fqn: String, psiElement: PsiElement? = null): PropertyOrBlockType? {
    val parts = fqn.split('.')
    if (parts.size < 2) return null
    val second = when (parts[0]) {
                   HCL_RESOURCE_IDENTIFIER -> {
                     getResourceType(parts[1], psiElement)
                   }

                   HCL_DATASOURCE_IDENTIFIER -> {
                     getDataSourceType(parts[1], psiElement)
                   }

                   else -> null
                 } ?: return null
    if (parts.size == 2) return second
    return find(second, parts.subList(2, parts.size))
  }

  private fun find(block: BlockType, parts: List<String>): PropertyOrBlockType? {
    if (parts.isEmpty()) return null
    val pobt = block.properties[parts[0]] ?: return null
    if (pobt is PropertyType) {
      return if (parts.size == 1) pobt else null
    }
    else if (pobt is BlockType) {
      return if (parts.size == 1) pobt else find(pobt, parts.subList(1, parts.size))
    }
    return null
  }

  fun allResources(): Sequence<ResourceType> = resourcesByProvider.values.asSequence().flatten()
  fun allDataSources(): Sequence<DataSourceType> = dataSourcesByProvider.values.asSequence().flatten()
  fun allProviders(): Sequence<ProviderType> = providersByFullName.values.asSequence()
  fun allEphemeralResources(): Sequence<EphemeralType> = ephemeralByProvider.values.asSequence().flatten()
}

fun Collection<PropertyOrBlockType>.toMap(): Map<String, PropertyOrBlockType> {
  val grouped: Map<String, List<PropertyOrBlockType>> = this.groupBy { it.name }
  val broken = HashSet<String>()
  for ((k, v) in grouped) {
    if (v.size > 1) broken.add(k)
  }
  if (broken.isNotEmpty()) {
    throw IllegalStateException("Grouping clash on keys: $broken")
  }
  return this.associateBy { it.name }
}
