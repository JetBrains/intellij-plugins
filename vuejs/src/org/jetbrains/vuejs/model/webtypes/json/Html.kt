// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("types-syntax", "tags", "attributes")
class Html {

  @JsonProperty("types-syntax")
  @get:JsonProperty("types-syntax")
  @set:JsonProperty("types-syntax")
  var typesSyntax: TypesSyntax? = null
  @JsonProperty("tags")
  @get:JsonProperty("tags")
  @set:JsonProperty("tags")
  var tags: List<Tag> = ArrayList()
  @JsonProperty("attributes")
  @get:JsonProperty("attributes")
  @set:JsonProperty("attributes")
  var attributes: List<Attribute_> = ArrayList()
  @JsonIgnore
  private val additionalProperties = HashMap<String, Any>()

  @JsonAnyGetter
  fun getAdditionalProperties(): Map<String, Any> {
    return this.additionalProperties
  }

  @JsonAnySetter
  fun setAdditionalProperty(name: String, value: Any) {
    this.additionalProperties[name] = value
  }

  enum class TypesSyntax(private val value: String) {

    TYPESCRIPT("typescript");

    override fun toString(): String {
      return this.value
    }

    @JsonValue
    fun value(): String {
      return this.value
    }

    companion object {
      private val CONSTANTS = HashMap<String, TypesSyntax>()

      init {
        for (c in values()) {
          CONSTANTS[c.value] = c
        }
      }

      @JsonCreator
      fun fromValue(value: String): TypesSyntax {
        val constant = CONSTANTS[value]
        return constant ?: throw IllegalArgumentException(value)
      }
    }

  }

}
