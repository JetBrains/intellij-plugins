// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("source-file", "attributes", "events", "slots", "vue-scoped-slots", "vue-model", "name", "description", "doc-url")
class Tag {

  @JsonProperty("source-file")
  @get:JsonProperty("source-file")
  @set:JsonProperty("source-file")
  var sourceFile: String? = null
  @JsonProperty("attributes")
  @get:JsonProperty("attributes")
  @set:JsonProperty("attributes")
  var attributes: List<Attribute> = ArrayList()
  @JsonProperty("events")
  @get:JsonProperty("events")
  @set:JsonProperty("events")
  var events: List<Event> = ArrayList()
  @JsonProperty("slots")
  @get:JsonProperty("slots")
  @set:JsonProperty("slots")
  var slots: List<Slot> = ArrayList()
  @JsonProperty("vue-scoped-slots")
  @get:JsonProperty("vue-scoped-slots")
  @set:JsonProperty("vue-scoped-slots")
  var vueScopedSlots: List<VueScopedSlot> = ArrayList()
  @JsonProperty("vue-model")
  @get:JsonProperty("vue-model")
  @set:JsonProperty("vue-model")
  var vueModel: VueModel? = null
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
