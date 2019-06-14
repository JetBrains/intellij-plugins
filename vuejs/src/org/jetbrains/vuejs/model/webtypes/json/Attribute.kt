// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("default", "required", "type", "name", "description", "doc-url")
class Attribute {

  @JsonProperty("default")
  @get:JsonProperty("default")
  @set:JsonProperty("default")
  var default: String? = null
  @JsonProperty("required")
  @get:JsonProperty("required")
  @set:JsonProperty("required")
  var required: Boolean? = null
  @JsonProperty("type")
  @get:JsonProperty("type")
  @set:JsonProperty("type")
  var type: Any? = null
  /**
   *
   * (Required)
   *
   */
  /**
   *
   * (Required)
   *
   */
  /**
   *
   * (Required)
   *
   */
  @JsonProperty("name")
  @get:JsonProperty("name")
  @set:JsonProperty("name")
  var name: String? = null
  @JsonProperty("description")
  @get:JsonProperty("description")
  @set:JsonProperty("description")
  var description: String? = null
  @JsonProperty("doc-url")
  @get:JsonProperty("doc-url")
  @set:JsonProperty("doc-url")
  var docUrl: String? = null
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

}
