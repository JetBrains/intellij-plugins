package com.jetbrains.swift.ift.lesson.editor

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftFoldingLesson : KLesson("swift.editorbasics.collapse", SwiftLessonsBundle.message("swift.editor.folding.name"),
                                   "Swift") {


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

    caret(13, 9)
    task {
      triggers("CollapseRegion")
      text(SwiftLessonsBundle.message("swift.editor.folding.collapse", action("CollapseRegion")))
    }
    task {
      triggers("ExpandRegion")
      text(SwiftLessonsBundle.message("swift.editor.folding.expand", action("ExpandRegion")))
    }
    task {
      triggers("CollapseAllRegions")
      text(SwiftLessonsBundle.message("swift.editor.folding.collapse.all", action("CollapseAllRegions")))
    }
    task {
      triggers("ExpandAllRegions")
      text(SwiftLessonsBundle.message("swift.editor.folding.expand.all", action("ExpandAllRegions")))
    }
    select(7, 9, 8, 24)
    task {
      triggers("CollapseSelection")
      text(SwiftLessonsBundle.message("swift.editor.folding.collapse.statement.any", action("CollapseSelection")))
    }
    task {
      triggers("ExpandRegion")
      text(SwiftLessonsBundle.message("swift.editor.folding.expand.statement", action("ExpandRegion")))
    }
    caret(18, 13)
    task {
      triggers("CollapseBlock")
      text(SwiftLessonsBundle.message("swift.editor.folding.block", action("CollapseBlock")))
    }
  }
}