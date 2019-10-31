/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn

import com.intellij.openapi.diagnostic.Logger
import org.jdom.Element
import org.jdom.JDOMException
import training.learn.exceptons.BadLessonException
import training.learn.exceptons.BadModuleException
import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.interfaces.ModuleType
import training.learn.lesson.Scenario
import training.learn.lesson.XmlLesson
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample
import training.util.DataLoader
import training.util.DataLoader.getResourceAsStream
import training.util.XmlModuleConstants
import training.util.isFeatureTrainerSnapshot
import java.io.IOException
import java.net.URISyntaxException
import java.util.*

/**
 * @author Sergey Karashevich
 */
class XmlModule(override val name: String,
                moduleXmlPath: String,
                private val root: Element,
                override val primaryLanguage: String?): Module {

  override val description: String?

  //used for lessons filtered by LangManger chosen lang
  override val lessons: ArrayList<Lesson> = ArrayList()
  override val sanitizedName: String
    get() = name.replace("[^\\dA-Za-z ]".toRegex(), "").replace("\\s+".toRegex(), "")
  override var id: String? = null
  override lateinit var moduleType: ModuleType

  private val answersPath: String?
  private var sdkType: ModuleSdkType?

  enum class ModuleSdkType {
    JAVA
  }

  init {
    val xroot = XRoot(root)
    description = xroot.valueNullable(XmlModuleConstants.MODULE_DESCRIPTION_ATTR)
    answersPath = xroot.valueNullable(XmlModuleConstants.MODULE_ANSWER_PATH_ATTR)
    id = xroot.valueNullable(XmlModuleConstants.MODULE_ID_ATTR)
    sdkType = getSdkTypeFromString(xroot.valueNullable(XmlModuleConstants.MODULE_SDK_TYPE))
    val fileTypeAttr = xroot.valueNotNull(XmlModuleConstants.MODULE_FILE_TYPE)
    moduleType = when {
      fileTypeAttr.toUpperCase() == ModuleType.SCRATCH.toString().toUpperCase() -> ModuleType.SCRATCH
      fileTypeAttr.toUpperCase() == ModuleType.PROJECT.toString().toUpperCase() -> ModuleType.PROJECT
      else -> throw BadModuleException("Unable to recognise ModuleType (should be SCRATCH or PROJECT)")
    }
    //path where module.xml is located and containing lesson dir
    val find = Regex("/[^/]*.xml").find(moduleXmlPath) ?: throw BadLessonException("Unable to parse a modules xml from '$moduleXmlPath'")
    val modulePath = moduleXmlPath.substring(0, find.range.start) + "/"
    initLessons(modulePath)
  }

  override fun toString(): String {
    return "($name for $primaryLanguage : $root)"
  }

  override fun giveNotPassedLesson(): Lesson? {
    return lessons.firstOrNull { !it.passed }
  }

  override fun giveNotPassedAndNotOpenedLesson(): Lesson? {
    return lessons.firstOrNull { !it.passed && !it.isOpen }
  }

  override fun hasNotPassedLesson(): Boolean {
    return lessons.any { !it.passed }
  }

  private fun initLessons(modulePath: String) {
    val lessonPathAttribute = root.getAttribute(XmlModuleConstants.MODULE_LESSONS_PATH_ATTR)

    //retrieve list of xml files inside lessonsPath directory
    val lessonsPath = if (lessonPathAttribute != null) modulePath + lessonPathAttribute.value
    else null

    for (lessonElement in root.children) {
      if (!isFeatureTrainerSnapshot && lessonElement.getAttributeValue(XmlModuleConstants.MODULE_LESSON_UNFINISHED_ATTR) == "true") {
        continue // do not show unfinished lessons in release
      }
      when (lessonElement.name) {
        XmlModuleConstants.MODULE_XML_LESSON_ELEMENT -> lessonsPath?.let {addXmlLesson(lessonElement, it) }
            ?: LOG.error("Need to specify ${XmlModuleConstants.MODULE_LESSONS_PATH_ATTR} in module attributes")
        XmlModuleConstants.MODULE_KT_LESSON_ELEMENT -> addKtLesson(lessonElement, lessonsPath)
        else -> LOG.error("Unknown element ${lessonElement.name} in  XmlModule file")
      }
    }
  }

  private fun addXmlLesson(lessonElement: Element, lessonsPath: String) {
    val lessonFilename = lessonElement.getAttributeValue(XmlModuleConstants.MODULE_LESSON_FILENAME_ATTR)
    val lessonPath = lessonsPath + lessonFilename
    try {
      val scenario = Scenario(lessonPath)
      val lesson = XmlLesson(scenario = scenario, lang = scenario.lang, module = this)
      lessons.add(lesson)
    } catch (e: JDOMException) {
      //XmlLesson file is corrupted
      LOG.error(BadLessonException("Probably lesson file is corrupted: $lessonPath JDOMException:$e"))
    } catch (e: IOException) {
      //XmlLesson file cannot be read
      LOG.error(BadLessonException("Probably lesson file cannot be read: " + lessonPath))
    }
  }

  private fun addKtLesson(lessonElement: Element, lessonsPath: String?) {
    val lessonImplementation = lessonElement.getAttributeValue(XmlModuleConstants.MODULE_LESSON_IMPLEMENTATION_ATTR)
    val lessonSampleName = lessonElement.getAttributeValue(XmlModuleConstants.MODULE_LESSON_SAMPLE_ATTR)

    val lesson : Any
    if (lessonSampleName != null) {
      if (lessonsPath == null) {
        LOG.error("Lesson $lessonImplementation requires sample $lessonSampleName but lessons path des not specified")
        return
      }
      val lessonLanguage = lessonElement.getAttributeValue(XmlModuleConstants.MODULE_LESSON_LANGUAGE_ATTR)
      val lessonConstructor = Class.forName(lessonImplementation).
          getDeclaredConstructor(Module::class.java, String::class.java, LessonSample::class.java)

      val content = getResourceAsStream(lessonsPath + lessonSampleName).readBytes().toString(Charsets.UTF_8)
      val sample = parseLessonSample(content)
      lesson = lessonConstructor.newInstance(this, lessonLanguage, sample)
    }
    else {
      val lessonConstructor = Class.forName(lessonImplementation).
          getDeclaredConstructor(Module::class.java)
      lesson = lessonConstructor.newInstance(this)
    }
    if (lesson !is KLesson) {
      LOG.error("Class $lessonImplementation specified in ${XmlModuleConstants.MODULE_LESSON_IMPLEMENTATION_ATTR} attribute should refer to existed Kotlin class")
      return
    }
    lessons.add(lesson)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as XmlModule

    if (name != other.name) return false
    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + (id?.hashCode() ?: 0)
    return result
  }

  companion object {

    @Throws(BadModuleException::class, BadLessonException::class, JDOMException::class, IOException::class, URISyntaxException::class)
    fun initModule(modulePath: String, primaryLanguage: String?): XmlModule? {
      //load xml with lessons

      //Check DOM with XmlModule
      val root = getRootFromPath(modulePath)
      if (root.getAttribute(XmlModuleConstants.MODULE_NAME_ATTR) == null) return null
      val name = root.getAttribute(XmlModuleConstants.MODULE_NAME_ATTR).value

      return XmlModule(name, modulePath, root, primaryLanguage)

    }

    @Throws(JDOMException::class, IOException::class)
    fun getRootFromPath(pathToFile: String): Element {
      return DataLoader.getXmlRootElement(pathToFile)
    }

    private fun getSdkTypeFromString(string: String?): ModuleSdkType? {
      if (string == null) return null
      for (moduleSdkType in ModuleSdkType.values()) {
        if (moduleSdkType.toString() == string) return moduleSdkType
      }
      return null
    }

    private val LOG = Logger.getInstance(XmlModule::class.java)
  }

  class XRoot(private val root: Element) {

    fun valueNotNull(attributeName: String): String {
      return root.getAttribute(attributeName)?.value ?: throw Exception("Unable to get attribute with name \"$attributeName\"")
    }

    fun valueNullable(attributeName: String): String? {
      return root.getAttribute(attributeName)?.value
    }
  }
}
