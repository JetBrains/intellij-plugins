// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("name", "description", "doc-url", "attributes", "source", "events", "slots", "vue-scoped-slots", "vue-model")
class Tag : SourceEntity {

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
  /**
   * Short description to be rendered in documentation popup. May contain HTML tags.
   *
   */
  /**
   * Short description to be rendered in documentation popup. May contain HTML tags.
   *
   */
  /**
   * Short description to be rendered in documentation popup. May contain HTML tags.
   *
   */
  @JsonProperty("description")
  @JsonPropertyDescription("Short description to be rendered in documentation popup. May contain HTML tags.")
  @get:JsonProperty("description")
  @set:JsonProperty("description")
  override var description: String? = null
  /**
   * Link to online documentation.
   *
   */
  /**
   * Link to online documentation.
   *
   */
  /**
   * Link to online documentation.
   *
   */
  @JsonProperty("doc-url")
  @JsonPropertyDescription("Link to online documentation.")
  @get:JsonProperty("doc-url")
  @set:JsonProperty("doc-url")
  override var docUrl: String? = null

  @JsonProperty("attributes")
  @get:JsonProperty("attributes")
  @set:JsonProperty("attributes")
  var attributes: List<Attribute> = ArrayList()
  /**
   * Allows to specify the source of the entity. For Vue.js component this may be for instance a class.
   *
   */
  /**
   * Allows to specify the source of the entity. For Vue.js component this may be for instance a class.
   *
   */
  /**
   * Allows to specify the source of the entity. For Vue.js component this may be for instance a class.
   *
   */
  @JsonProperty("source")
  @JsonPropertyDescription("Allows to specify the source of the entity. For Vue.js component this may be for instance a class.")
  @get:JsonProperty("source")
  @set:JsonProperty("source")
  override var source: Source? = null
  /**
   *
   *
   */
  /**
   *
   *
   */
  /**
   *
   *
   */
  @JsonProperty("events")
  @JsonPropertyDescription("")
  @get:JsonProperty("events")
  @set:JsonProperty("events")
  var events: List<Event> = ArrayList()

  @JsonProperty("slots")
  @get:JsonProperty("slots")
  @set:JsonProperty("slots")
  var slots: List<Slot> = ArrayList()
  /**
   * Deprecated. Use regular 'slot' property instead and specify 'vue-properties' to provide slot scope information.
   *
   */
  /**
   * Deprecated. Use regular 'slot' property instead and specify 'vue-properties' to provide slot scope information.
   *
   */
  /**
   * Deprecated. Use regular 'slot' property instead and specify 'vue-properties' to provide slot scope information.
   *
   */
  @JsonProperty("vue-scoped-slots")
  @JsonPropertyDescription(
    "Deprecated. Use regular 'slot' property instead and specify 'vue-properties' to provide slot scope information.")
  @get:JsonProperty("vue-scoped-slots")
  @set:JsonProperty("vue-scoped-slots")
  var vueScopedSlots: List<Slot> = ArrayList()

  @JsonProperty("vue-model")
  @get:JsonProperty("vue-model")
  @set:JsonProperty("vue-model")
  var vueModel: VueModel? = null

}
