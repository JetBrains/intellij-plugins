/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.migration

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import org.jdom.Element
import org.jdom.input.SAXBuilder
import training.learn.lesson.LessonState
import java.io.File
import java.io.FileInputStream
import java.util.*

class MigrationAgent074: MigrationManager.MigrationAgent() {

  override val VERSION: String
    get() = "0.7.49"

  override val XML_FILE_NAME: String
    get() = "trainingPlugin"

  private fun getFileForVersion074(): File {
    return PathManager.getOptionsFile(XML_FILE_NAME)
  }

  private fun getFileRoot(): Element? {
    try {
      val xmlFile = getFileForVersion074()
      if (!xmlFile.exists()) return null
      val inputStream = FileInputStream(xmlFile)
      val builder = SAXBuilder()
      return builder.build(inputStream).document.rootElement ?: throw Exception("Unable to build document from file input stream or root is null")
    } catch (e: Exception) {
      LOG.warn("Unable to migrate lessons state data, because: ", e)
    }
    return null
  }

  override fun extractLessonStateMap(): Map<String, LessonState> {
    val map = HashMap<String, LessonState>()
    val root = getFileRoot() ?: return emptyMap()
    val componentTrainingPluginModules = root.getChild("name", "TrainingPluginModules")
    val modules = componentTrainingPluginModules.getChild("name", "modules")
    val courses = modules.getChild("list")
    courses.children.forEach {
      val moduleName = it.getChild("name", "name").getAttributeValue("value")
      val lessons = it.getChild("name", "lessons")
      val lessonsList = lessons.getChild("list")
      lessonsList.children.forEach {
        val lessonName = it.getChild("name", "name").getAttributeValue("value")
        val isPassed: Boolean = (it.getChildNullable("name", "passed")?.getAttributeValue("value") ?: "false").toBoolean()
        if (isPassed) map[createId(moduleName, lessonName)] = if (isPassed) LessonState.PASSED else LessonState.NOT_PASSED
      }
    }
    return map
  }

  private fun Element.getChild(key: String, value: String): Element {
    return this.children.find { it.getAttribute(key) != null && it.getAttributeValue(key) == value } ?: throw Exception("Unable to find component element with attribute: $key = \"$value\"")
  }

  private fun Element.getChildNullable(key: String, value: String): Element? {
    return this.children.find { it.getAttribute(key) != null && it.getAttributeValue(key) == value }
  }

  private fun createId(moduleName: String, lessonName: String): String {
    return "${moduleName.withoutWhiteSpaces()}.${lessonName.withoutWhiteSpaces()}"
  }

  private fun String.withoutWhiteSpaces() = this.replace("\\s+".toRegex(), "")

  companion object {
    val LOG = Logger.getInstance(MigrationAgent074::class.java)
  }
}