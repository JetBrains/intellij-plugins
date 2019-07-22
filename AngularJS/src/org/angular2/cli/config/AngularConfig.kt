// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.CharSequenceReader
import one.util.streamex.StreamEx

class AngularConfig(val angularJsonFile: VirtualFile, text: CharSequence) {

  val projects: List<AngularProject>

  val defaultProject: AngularProject?

  init {
    val angularCliFolder = angularJsonFile.parent
    val mapper = ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val angularJson = mapper.readValue(CharSequenceReader(text), AngularJson::class.java)
    if (angularJson.projects.isNotEmpty()) {
      projects = angularJson.projects.map { (name, project) ->
        AngularProject(name, project, angularCliFolder)
      }
    }
    else {
      projects = angularJson.apps.map {
        AngularProject(it.name ?: "app", it, angularCliFolder)
      }
    }
    defaultProject = angularJson.defaultProject?.let { defaultProject ->
      projects.find { it.name == defaultProject }
    }
  }

  fun getProject(context: VirtualFile): AngularProject? {
    return StreamEx.of(projects)
      .map { Pair(it, it.proximity(context)) }
      .filter { it.second >= 0 }
      .minByInt { it.second }
      .map { it.first }
      .orElse(null)
  }

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
