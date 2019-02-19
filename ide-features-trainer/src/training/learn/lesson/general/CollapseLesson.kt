package training.learn.lesson.general

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample

class CollapseLesson(module: Module, lang: String, private val sample: LessonSample) :
    KLesson("Collapse", module, lang) {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)

      triggerTask("CollapseRegion") {
        text("Sometimes you need to collapse a piece of code for better readability. Try collapsing code with ${action(it)}.")
      }
      triggerTask("ExpandRegion") {
        text("To expand a code region, hit ${action(it)}")
      }
      triggerTask("CollapseAllRegions") {
        text("If you want to collapse all regions in the file, use ${action(it)}")
      }
      triggerTask("ExpandAllRegions") {
        text("Similarly, press ${action(it)} to expand all available regions")
      }
    }
}