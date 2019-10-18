// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("name", "pattern", "description", "doc-url", "vue-properties")
class Slot : DocumentedItem {

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
   * A RegEx pattern to match whole content. Syntax should work with at least ECMA, Java and Python implementations.
   *
   */
  /**
   * A RegEx pattern to match whole content. Syntax should work with at least ECMA, Java and Python implementations.
   *
   */
  /**
   * A RegEx pattern to match whole content. Syntax should work with at least ECMA, Java and Python implementations.
   *
   */
  @JsonProperty("pattern")
  @JsonPropertyDescription(
    "A RegEx pattern to match whole content. Syntax should work with at least ECMA, Java and Python implementations.")
  @get:JsonProperty("pattern")
  @set:JsonProperty("pattern")
  var pattern: Any? = null
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
  /**
   * Specify properties of the slot scope
   *
   */
  /**
   * Specify properties of the slot scope
   *
   */
  /**
   * Specify properties of the slot scope
   *
   */
  @JsonProperty("vue-properties")
  @JsonAlias("properties")
  @JsonPropertyDescription("Specify properties of the slot scope")
  @get:JsonProperty("vue-properties")
  @set:JsonProperty("vue-properties")
  var vueProperties: List<VueProperty> = ArrayList()

}
