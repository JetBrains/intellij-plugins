// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("name", "description", "doc-url", "default", "required", "value", "type")
class Attribute : DocumentedItem {

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

  @JsonProperty("default")
  @get:JsonProperty("default")
  @set:JsonProperty("default")
  var default: String? = null

  @JsonProperty("required")
  @get:JsonProperty("required")
  @set:JsonProperty("required")
  var required: Boolean? = null

  @JsonProperty("value")
  @get:JsonProperty("value")
  @set:JsonProperty("value")
  var value: Any? = null
  /**
   * Deprecated. Use 'value' property instead. Specify only if type is 'boolean' for compatibility with WebStorm 2019.2.
   *
   */
  /**
   * Deprecated. Use 'value' property instead. Specify only if type is 'boolean' for compatibility with WebStorm 2019.2.
   *
   */
  /**
   * Deprecated. Use 'value' property instead. Specify only if type is 'boolean' for compatibility with WebStorm 2019.2.
   *
   */
  @JsonProperty("type")
  @JsonPropertyDescription(
    "Deprecated. Use 'value' property instead. Specify only if type is 'boolean' for compatibility with WebStorm 2019.2.")
  @get:JsonProperty("type")
  @set:JsonProperty("type")
  var type: Any? = null

}
