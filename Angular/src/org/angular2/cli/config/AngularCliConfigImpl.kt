// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.CharSequenceReader

class AngularCliConfigImpl(text: CharSequence, override val file: VirtualFile) : AngularConfig {

  override val projects: List<AngularProject>

  override val defaultProject: AngularProject?

  init {
    val angularCliFolder = file.parent
    val mapper = ObjectMapper(
      JsonFactory.builder()
        .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
        .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)
        .configure(JsonReadFeature.ALLOW_MISSING_VALUES, true)
        .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
        .configure(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .build())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
    val angularJson = mapper.readValue(CharSequenceReader(text), AngularJson::class.java)
    if (angularJson.projects.isNotEmpty()) {
      projects = angularJson.projects.map { (name, ngProjectJson) ->
        AngularProjectImpl(name, ngProjectJson, angularCliFolder)
      }
      defaultProject = angularJson.defaultProject?.let { defaultProject ->
        projects.find { it.name == defaultProject }
      }
    }
    else {
      projects = angularJson.legacyApps.map { app ->
        AngularLegacyProjectImpl(angularJson, app, angularCliFolder)
      }
      defaultProject = projects.firstOrNull()
    }
  }

  override fun getProject(context: VirtualFile): AngularProject? =
    projects
      .map { Pair(it, it.proximity(context)) }
      .filter { it.second >= 0 }
      .minByOrNull { it.second }
      ?.first

  override fun toString(): String {
    return """
      | AngularConfig {
      |   defaultProject: ${defaultProject?.name}
      |   projects: [
      |     ${projects.joinToString(",\n     ") { it.toString() }}
      |   ]
      | }
    """.trimMargin()
  }

}
