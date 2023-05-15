// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.util.Pair
import com.intellij.util.asSafely

object MetadataUtils {

  fun listObjectProperties(property: JsonProperty?): List<JsonProperty> {
    return if (property == null || property.value !is JsonObject) {
      emptyList()
    }
    else (property.value as JsonObject).propertyList
  }

  fun readStringProperty(property: JsonProperty?): Pair<String, String>? =
    property?.value?.asSafely<JsonStringLiteral>()?.value?.let {
      Pair(property.name, it)
    }

  fun readStringPropertyValue(property: JsonProperty?): String? =
    property?.value?.asSafely<JsonStringLiteral>()?.value

  inline fun <reified T : JsonValue> getPropertyValue(property: JsonProperty?): T? =
    property?.value as? T
}
