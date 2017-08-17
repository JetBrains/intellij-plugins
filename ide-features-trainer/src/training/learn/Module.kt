package training.learn

import org.jdom.Element
import org.jdom.JDOMException
import training.lang.LangManager
import training.lang.LangSupport
import training.learn.exceptons.BadLessonException
import training.learn.exceptons.BadModuleException
import training.learn.lesson.Lesson
import training.learn.lesson.Scenario
import training.util.DataLoader
import training.util.GenModuleXml
import training.util.GenModuleXml.*
import java.io.IOException
import java.net.URISyntaxException
import java.util.*
import java.util.function.Consumer

/**
 * @author Sergey Karashevich
 */
class Module(val name: String, val moduleXmlPath: String, val modulesPath: String, private val root: Element) {

  val description: String?

  enum class ModuleType {
    SCRATCH, PROJECT
  }

  //used for lessons filtered by LangManger chosen lang
  var lessons: List<Lesson> = ArrayList<Lesson>()
  private val allLessons = ArrayList<Lesson>()
  private val moduleUpdateListeners = ArrayList<ModuleUpdateListener>()

  val answersPath: String?
  var id: String?
  var moduleType: ModuleType
  var sdkType: ModuleSdkType?

  enum class ModuleSdkType {
    JAVA
  }

  init {
    val xroot = XRoot(root)
    description = xroot.valueNullable(MODULE_DESCRIPTION_ATTR)
    answersPath = xroot.valueNullable(MODULE_ANSWER_PATH_ATTR)
    id = xroot.valueNullable(MODULE_ID_ATTR)
    sdkType = getSdkTypeFromString(xroot.valueNullable(MODULE_SDK_TYPE))
    val fileTypeAttr = xroot.valueNotNull(MODULE_FILE_TYPE)
    moduleType = when {
      fileTypeAttr.toUpperCase() == ModuleType.SCRATCH.toString().toUpperCase() -> ModuleType.SCRATCH
      fileTypeAttr.toUpperCase() == ModuleType.PROJECT.toString().toUpperCase() -> ModuleType.PROJECT
      else -> throw BadModuleException("Unable to recognise ModuleType (should be SCRATCH or PROJECT)")
    }
    initLessons()
  }

  private fun initLessons() {

    if (root.getAttribute(MODULE_LESSONS_PATH_ATTR) != null) {

      //retrieve list of xml files inside lessonsPath directory
      val lessonsPath = modulesPath + root.getAttribute(MODULE_LESSONS_PATH_ATTR).value

      for (lessonElement in root.children) {
        if (lessonElement.name != MODULE_LESSON_ELEMENT)
          throw BadModuleException("Module file is corrupted or cannot be read properly")

        val lessonFilename = lessonElement.getAttributeValue(MODULE_LESSON_FILENAME_ATTR)
        val lessonPath = lessonsPath + lessonFilename
        try {
          val scenario = Scenario(lessonPath)
          val lesson = Lesson(scenario = scenario, lang = scenario.lang, module = this)
          allLessons.add(lesson)
        } catch (e: JDOMException) {
          //Lesson file is corrupted
          throw BadLessonException("Probably lesson file is corrupted: $lessonPath JDOMException:$e")
        } catch (e: IOException) {
          //Lesson file cannot be read
          throw BadLessonException("Probably lesson file cannot be read: " + lessonPath)
        }
      }
    }
    lessons = filterLessonsByCurrentLang()
  }

  fun setMySdkType(mySdkType: ModuleSdkType?) {
    this.sdkType = mySdkType
  }


  private fun filterLessonsByCurrentLang(): List<Lesson> {
    val langManager = LangManager.getInstance()
    if (langManager.isLangUndefined()) return allLessons
    return filterLessonByLang(langManager.getLangSupport())
  }

  fun filterLessonByLang(langSupport: LangSupport): List<Lesson> {
    return allLessons.filter { langSupport.acceptLang(it.lang) }
  }

  fun giveNotPassedLesson(): Lesson? {
    return lessons.firstOrNull { !it.passed }
  }

  fun giveNotPassedAndNotOpenedLesson(): Lesson? {
    return lessons.firstOrNull { !it.passed && !it.isOpen }
  }

  fun hasNotPassedLesson(): Boolean {
    return lessons.none { it.passed }
  }

  val nameWithoutWhitespaces: String
    get() = name.replace("\\s+".toRegex(), "")

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (other !is Module) return false
    return other.name == this.name

  }

  fun update() {
    lessons = filterLessonsByCurrentLang()
    moduleUpdateListeners.forEach(Consumer<ModuleUpdateListener> { it.onUpdate() })
  }

  fun registerListener(moduleUpdateListener: ModuleUpdateListener) {
    moduleUpdateListeners.add(moduleUpdateListener)
  }

  fun removeAllListeners() {
    moduleUpdateListeners.clear()
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + root.hashCode()
    result = 31 * result + (id?.hashCode() ?: 0)
    return result
  }

  inner class ModuleUpdateListener : EventListener {
    internal fun onUpdate() {}
  }

  companion object {

    @Throws(BadModuleException::class, BadLessonException::class, JDOMException::class, IOException::class, URISyntaxException::class)
    fun initModule(modulePath: String, modulesHomePath: String): Module? {
      //load xml with lessons

      //Check DOM with Module
      val root = getRootFromPath(modulePath)
      if (root.getAttribute(GenModuleXml.MODULE_NAME_ATTR) == null) return null
      val name = root.getAttribute(GenModuleXml.MODULE_NAME_ATTR).value

      return Module(name, modulePath, modulesHomePath, root)

    }

    @Throws(JDOMException::class, IOException::class)
    fun getRootFromPath(pathToFile: String): Element {
      return DataLoader.getXmlRootElement(pathToFile)
    }
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
