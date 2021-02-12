package com.jetbrains.swift.ift.lesson.codegeneration

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftCreateFromUsageLesson : KLesson("swift.codegeneration.createfromusage",
                                           SwiftLessonsBundle.message("swift.codegeneration.cfu.name")) {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class CreateFromUsage: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addSubview(label)
        setup(view:label)
    }

    func createClassFromUsage() {
        var ide = IDE()

        var anotherIDE = IDE(helps:true)
    }
}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    caret(7, 30)
    task {
      triggers("ShowIntentionActions", "EditorChooseLookupItem")
      text(SwiftLessonsBundle.message("swift.codegeneration.cfu.create.local", action("ShowIntentionActions"), action("ShowIntentionActions"), LessonUtil.rawEnter()))
    }
    task { type(" = UILabel()") }

    caret(9, 11)
    task {
      triggers("ShowIntentionActions", "NextTemplateVariable")
      text(SwiftLessonsBundle.message("swift.codegeneration.cfu.repeat", code("setup")))
    }
    text(SwiftLessonsBundle.message("swift.codegeneration.cfu.nice"))
    caret(17, 20)
    task {
      triggers("ShowIntentionActions", "EditorChooseLookupItem")
      text(SwiftLessonsBundle.message("swift.codegeneration.cfu.create.class", action("ShowIntentionActions")))
    }
    caret(22, 27)
    task {
      triggers("ShowIntentionActions", "NextTemplateVariable")
      text(SwiftLessonsBundle.message("swift.codegeneration.cfu.create.init", code("IDE"), action("ShowIntentionActions"), LessonUtil.rawEnter()))
    }
    caret(22, 21)
    task {
      triggers("ShowIntentionActions")
      text(SwiftLessonsBundle.message("swift.codegeneration.cfu.create.empty.init"))
    }
  }
}