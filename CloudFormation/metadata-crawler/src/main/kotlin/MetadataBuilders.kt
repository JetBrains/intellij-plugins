import com.intellij.aws.cloudformation.metadata.CloudFormationResourceAttribute
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceProperty
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypeDescription

private val nameRegex = Regex("^[a-zA-Z0-9.]+$")

class ResourceTypePropertyBuilder(val name: String) {
  init {
    if (!nameRegex.matches(name)) {
      error("ResourceTypePropertyName is invalid: $name")
    }
  }

  var type: String? = null
  var required: Boolean? = null
  var description: String? = null
  var url: String? = null
  var updateRequires: String? = null

  fun toResourceTypeProperty() =
    CloudFormationResourceProperty(
        name = name,
        required = required ?: error("'required' is not set is not set for property $name"),
        type = type ?: error("'type' is not set  is not set for property $name"),
        updateRequires = updateRequires ?: error("'updateRequires' is not set for property $name"),
        url = url ?: error("'url' is not set  is not set for property $name")
    )
}

class ResourceTypeAttributeBuilder(val name: String) {
  init {
    if (!nameRegex.matches(name)) {
      error("ResourceTypeAttributeName is invalid: $name")
    }
  }

  var description: String? = null

  fun toResourceTypeAttribute() = CloudFormationResourceAttribute(name)
}

class ResourceTypeBuilder(val name: String, val url: String) {
  private val resourceTypeNameRegex = Regex("^([a-zA-Z0-9]|::)+$")

  init {
    if (!resourceTypeNameRegex.matches(name)) {
      error("ResourceTypeName is invalid: $name")
    }
  }

  var description: String? = null
  var transform: String? = null

  private val properties = mutableMapOf<String, ResourceTypePropertyBuilder>()
  private val attributes = mutableMapOf<String, ResourceTypeAttributeBuilder>()

  fun addProperty(name: String): ResourceTypePropertyBuilder =
      properties.getOrPut(name) { ResourceTypePropertyBuilder(name) }

  fun addAttribute(name: String): ResourceTypeAttributeBuilder =
      attributes.getOrPut(name) { ResourceTypeAttributeBuilder(name) }

  fun toResourceType() =
    CloudFormationResourceType(
        name = name,
        transform = transform,
        url = url,
        attributes = attributes.mapValues { it.value.toResourceTypeAttribute() },
        properties = properties.mapValues { it.value.toResourceTypeProperty() }
    )

  fun toResourceTypeDescription() =
      CloudFormationResourceTypeDescription(
          description = description ?: error("'description' is not set"),
          properties = properties.mapValues { it.value.description ?: error("'description' is not set in property ${it.value.name} of resource type $name") },
          attributes = attributes.mapValues { it.value.description ?: error("'description' is not set in attribute ${it.value.name} of resource type $name") }
      )
}