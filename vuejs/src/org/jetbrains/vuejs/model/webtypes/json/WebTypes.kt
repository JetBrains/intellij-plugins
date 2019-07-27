// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.*
import java.util.*


/**
 * web-types
 *
 *
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("framework", "name", "version", "contributions")
class WebTypes {

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
  @JsonProperty("framework")
  @get:JsonProperty("framework")
  @set:JsonProperty("framework")
  var framework: Framework? = null
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
  @JsonProperty("version")
  @get:JsonProperty("version")
  @set:JsonProperty("version")
  var version: String? = null
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
  @JsonProperty("contributions")
  @get:JsonProperty("contributions")
  @set:JsonProperty("contributions")
  var contributions: Contributions? = null
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

  enum class Framework(private val value: String) {

    VUE("vue");

    override fun toString(): String {
      return this.value
    }

    @JsonValue
    fun value(): String {
      return this.value
    }

    companion object {
      private val CONSTANTS = HashMap<String, Framework>()

      init {
        for (c in values()) {
          CONSTANTS[c.value] = c
        }
      }

      @JsonCreator
      fun fromValue(value: String): Framework {
        val constant = CONSTANTS[value]
        return constant ?: throw IllegalArgumentException(value)
      }
    }

  }

}
