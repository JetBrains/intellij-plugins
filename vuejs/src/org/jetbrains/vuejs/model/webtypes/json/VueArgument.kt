// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 * Provide information about directive argument
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("pattern", "description", "doc-url", "required")
class VueArgument : DocumentedItem {

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
   * Short description to be rendered in documentation popup. It will be rendered according to description-markup setting.
   *
   */
  /**
   * Short description to be rendered in documentation popup. It will be rendered according to description-markup setting.
   *
   */
  /**
   * Short description to be rendered in documentation popup. It will be rendered according to description-markup setting.
   *
   */
  @JsonProperty("description")
  @JsonPropertyDescription(
    "Short description to be rendered in documentation popup. It will be rendered according to description-markup setting.")
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
   * Whether directive requires an argument
   *
   */
  /**
   * Whether directive requires an argument
   *
   */
  /**
   * Whether directive requires an argument
   *
   */
  @JsonProperty("required")
  @JsonPropertyDescription("Whether directive requires an argument")
  @get:JsonProperty("required")
  @set:JsonProperty("required")
  var required: Boolean? = false

}
