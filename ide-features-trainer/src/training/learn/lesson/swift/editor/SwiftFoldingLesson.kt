package training.learn.lesson.swift.editor

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftFoldingLesson(module: Module) : KLesson("swift.editorbasics.collapse", "Folding", module, "Swift") {


  private val sample: LessonSample = parseLessonSample("""
import Foundation

class FoldingDemo {

    public class func process(register: Int) {

        var counter = Array<Int>()
        counter[0] = 0;
        var closure = {
            counter[0] = counter[0] + 1;
            print("\(counter[0]) ");
        }
        checkAndStart(flag: (register > 10), closure: closure)
    }

    private class func checkAndStart(flag: Bool, closure: () -> ()) {
        if (flag) {
            closure()
        } else {
            print("suspending");
        }
    }
}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    task { caret(13, 9) }
    task {
      triggers("CollapseRegion")
      text("Sometimes you need to collapse a piece of code for better readability. Try collapsing code with ${action("CollapseRegion")}.")
    }
    task {
      triggers("ExpandRegion")
      text("To expand a code region, press ${action("ExpandRegion")}.")
    }
    task {
      triggers("CollapseAllRegions")
      text("If you want to collapse all regions in the file, use ${action("CollapseAllRegions")}.")
    }
    task {
      triggers("ExpandAllRegions")
      text("Similarly, press ${action("ExpandAllRegions")} to expand all available regions.")
    }
    task { caret(10, 13) }
    task { select(7, 9, 8, 24) }

    task { caret(8, 24) }
    task {
      triggers("CollapseSelection")
      text("In <ide/>, there's a way to fold any sequence of statements or declarations. Try folding the selected region with ${action("CollapseSelection")}.")
    }
    task {
      triggers("ExpandRegion")
      text("Now press ${action("ExpandRegion")} to go the other way.")
    }
    task { caret(18, 13) }
    task {
      triggers("CollapseBlock")
      text("And finally, use ${action("CollapseBlock")} to fold the control flow statements.")
    }
  }
}