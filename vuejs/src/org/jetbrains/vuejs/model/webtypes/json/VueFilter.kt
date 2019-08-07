// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("name", "description", "doc-url", "accepts", "returns", "arguments")
class VueFilter {

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
  var description: String? = null
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
  var docUrl: String? = null
  /**
   * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
   *
   */
  /**
   * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
   *
   */
  /**
   * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
   *
   */
  @JsonProperty("accepts")
  @JsonPropertyDescription(
    "Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.")
  @get:JsonProperty("accepts")
  @set:JsonProperty("accepts")
  var accepts: Any? = null
  /**
   * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
   *
   */
  /**
   * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
   *
   */
  /**
   * Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.
   *
   */
  @JsonProperty("returns")
  @JsonPropertyDescription(
    "Specify type according to selected language for type syntax. The type can be specified by a string expression, an object with list of imports and an expression, or an array of possible types.")
  @get:JsonProperty("returns")
  @set:JsonProperty("returns")
  var returns: Any? = null
  /**
   * List of arguments accepted by the filter. All arguments are non-optional by default.
   *
   */
  /**
   * List of arguments accepted by the filter. All arguments are non-optional by default.
   *
   */
  /**
   * List of arguments accepted by the filter. All arguments are non-optional by default.
   *
   */
  @JsonProperty("arguments")
  @JsonPropertyDescription("List of arguments accepted by the filter. All arguments are non-optional by default.")
  @get:JsonProperty("arguments")
  @set:JsonProperty("arguments")
  var arguments: List<Argument_> = ArrayList()

}
