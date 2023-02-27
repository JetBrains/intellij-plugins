// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

class TypeModel(
  resources: List<ResourceType> = emptyList(),
  dataSources: List<DataSourceType> = emptyList(),
  providers: List<ProviderType> = emptyList(),
  provisioners: List<ProvisionerType> = emptyList(),
  backends: List<BackendType> = emptyList(),
  functions: List<Function> = emptyList()
) {

  val resources: List<ResourceType> = resources.sortedBy { it.type }
  val dataSources: List<DataSourceType> = dataSources.sortedBy { it.type }
  val providers: List<ProviderType> = providers.sortedBy { it.type }
  val provisioners: List<ProvisionerType> = provisioners.sortedBy { it.type }
  val backends: List<BackendType> = backends.sortedBy { it.type }
  val functions: List<Function> = functions.sortedBy { it.name }

  @Suppress("MemberVisibilityCanBePrivate")
  companion object {
    private val VersionProperty = PropertyType("version", Types.String, hint = SimpleHint("VersionRange"), injectionAllowed = false)
    val TerraformRequiredVersion = PropertyType("required_version", Types.String, hint = SimpleHint("VersionRange"),
                                                injectionAllowed = false)

    val DependsOnProperty = PropertyType("depends_on", Types.Array,
                                         hint = ReferenceHint("resource.#name", "data_source.#name", "module.#name", "variable.#name"))

    val Atlas: BlockType = BlockType("atlas", 0, properties = listOf(
      PropertyType("name", Types.String, injectionAllowed = false, required = true)).toMap())
    val Module: BlockType = BlockType("module", 1, properties = listOf(
      PropertyType("source", Types.String, hint = SimpleHint("Url"), required = true),
      VersionProperty,
      DependsOnProperty,
      PropertyType("count", Types.Number, conflictsWith = listOf("for_each")),
      PropertyType("for_each", Types.Any, conflictsWith = listOf("count")),
      PropertyType("providers", MapType(Types.String))
    ).toMap())
    val Output: BlockType = BlockType("output", 1, properties = listOf(
      PropertyType("value", Types.Any, required = true),
      DependsOnProperty,
      PropertyType("sensitive", Types.Boolean)
    ).toMap())

    val Variable_Type = PropertyType("type", Types.Any, injectionAllowed = false)
    val Variable_Default = PropertyType("default", Types.Any)
    val Variable_Description = PropertyType("description", Types.String)
    val Variable_Validation = BlockType("validation", 0, properties = listOf(
      PropertyType("condition", Types.Boolean, injectionAllowed = false),
      PropertyType("error_message", Types.String)
    ).toMap())
    val Variable: BlockType = BlockType("variable", 1, properties = listOf<PropertyOrBlockType>(
      Variable_Type,
      Variable_Default,
      Variable_Validation,
      Variable_Description
    ).toMap())

    val Connection: BlockType = BlockType("connection", 0, properties = listOf(
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

    val ResourceLifecycle: BlockType = BlockType("lifecycle", 0,
                                                 description = "Describe to Terraform how to connect to the resource for provisioning", // TODO: Improve description
                                                 properties = listOf(
                                                   PropertyType("create_before_destroy", Types.Boolean),
                                                   PropertyType("prevent_destroy", Types.Boolean),
                                                   PropertyType("ignore_changes", ListType(Types.Any))
                                                 ).toMap())
    val AbstractResourceProvisioner: BlockType = BlockType("provisioner", 1, properties = listOf(
      Connection
    ).toMap())

    val AbstractResourceDynamicContent: BlockType = BlockType("content", 0, required = true)
    val ResourceDynamic: BlockType = BlockType("dynamic", 1, properties = listOf<PropertyOrBlockType>(
      PropertyType("for_each", Types.Any, required = true),
      PropertyType("labels", Types.Array),
      PropertyType("iterator", Types.Identifier),
      AbstractResourceDynamicContent
    ).toMap())

    @JvmField
    val AbstractResource: BlockType = BlockType("resource", 2, properties = listOf<PropertyOrBlockType>(
      PropertyType("id", Types.String, injectionAllowed = false, description = "A unique ID for this resource", optional = false,
                   required = false, computed = true),
      PropertyType("count", Types.Number, conflictsWith = listOf("for_each")),
      PropertyType("for_each", Types.Any, conflictsWith = listOf("count")),
      DependsOnProperty,
      PropertyType("provider", Types.String, hint = ReferenceHint("provider.#type", "provider.#alias")),
      ResourceLifecycle,
      ResourceDynamic,
      // Also may have connection? and provisioner+ blocks
      Connection,
      AbstractResourceProvisioner
    ).toMap())

    @JvmField
    val AbstractDataSource: BlockType = BlockType("data", 2, properties = listOf(
      PropertyType("id", Types.String, injectionAllowed = false, description = "A unique ID for this data source", optional = false,
                   required = false, computed = true),
      PropertyType("count", Types.Number, conflictsWith = listOf("for_each")),
      PropertyType("for_each", Types.Any, conflictsWith = listOf("count")),
      DependsOnProperty,
      PropertyType("provider", Types.String, hint = ReferenceHint("provider.#type", "provider.#alias"))
    ).toMap())

    @JvmField
    val AbstractProvider: BlockType = BlockType("provider", 1, required = false, properties = listOf(
      PropertyType("alias", Types.String, injectionAllowed = false),
      VersionProperty
    ).toMap())
    val AbstractBackend: BlockType = BlockType("backend", 1)
    val Moved: BlockType = BlockType("moved", properties = listOf(
      PropertyType("from", Types.Identifier, required = true),
      PropertyType("to", Types.Identifier, required = true)
    ).toMap())
    val Cloud: BlockType = BlockType("cloud", properties = listOf(
      PropertyType("organization", Types.String),
      BlockType("workspaces", required = true, properties = listOf(
        PropertyType("name", Types.String, conflictsWith = listOf("tags")),
        PropertyType("tags", ListType(Types.String), conflictsWith = listOf("name"))
      ).toMap())
    ).toMap())
    val Terraform: BlockType = BlockType("terraform", properties = listOf<PropertyOrBlockType>(
      TerraformRequiredVersion,
      PropertyType("experiments", Types.Array),
      BlockType("required_providers"),
      Cloud,
      AbstractBackend
    ).toMap())
    val Locals: BlockType = BlockType("locals")

    val RootBlocks = listOf(Atlas, Module, Output, Variable, AbstractProvider, AbstractResource, AbstractDataSource, Terraform, Locals,
                            Moved)
    val RootBlocksMap = RootBlocks.map { it.literal to it }.toMap()
  }

  private fun <T> List<T>.findBinary(elt: String, k: (T) -> String): T? =
    findIndexBinary(elt, k).takeIf { it != -1 }?.let { this[it] }

  private fun <T> List<T>.findIndexBinary(elt: String, k: (T) -> String): Int {
    var low = 0
    var high: Int = this.size - 1

    while (low <= high) {
      val mid = (low + high) ushr 1
      val midVal: T = this.get(mid)
      val cmp: Int = k(midVal).compareTo(elt)
      when {
        cmp < 0 -> low = mid + 1
        cmp > 0 -> high = mid - 1
        else -> return mid
      } 
    }
    return -1
  }

  fun getResourceType(name: String): ResourceType? {
    return resources.findBinary(name) { it.type }
  }

  fun getDataSourceType(name: String): DataSourceType? {
    return dataSources.findBinary(name) { it.type }
  }

  fun getProviderType(name: String): ProviderType? {
    return providers.findBinary(name) { it.type }
  }

  fun getProvisionerType(name: String): ProvisionerType? {
    return provisioners.findBinary(name) { it.type }
  }

  fun getBackendType(name: String): BackendType? {
    return backends.findBinary(name) { it.type }
  }

  fun getFunction(name: String): Function? {
    return functions.findBinary(name) { it.name }
  }

  fun getByFQN(fqn: String): PropertyOrBlockType? {
    val parts = fqn.split('.')
    if (parts.size < 2) return null
    val second = when (parts[0]) {
                   "resource" -> {
                     getResourceType(parts[1])
                   }

                   "data" -> {
                     getDataSourceType(parts[1])
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
  return this.map { it.name to it }.toMap()
}
