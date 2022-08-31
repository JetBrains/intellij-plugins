package com.jetbrains.swift.ift.lesson.rundebugtest

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftDebugLesson : KLesson("swift.rdt.debug", SwiftLessonsBundle.message("swift.rdt.debug.name")) {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class DebugExample: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let x = 0
        let y = 50

        let tableView = UITableView()

        let header = UILabel()
        header.text = "AppCode"
        header.sizeToFit()

        tableView.frame = CGRect(x: x, y: y, width: 320, height: 400)
        tableView.tableHeaderView = header
        self.view.addSubview(tableView)
    }
}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    text(SwiftLessonsBundle.message("swift.rdt.debug.intro"))
    task {
      triggers("GotoFile", "MasterViewController.swift")
      text(SwiftLessonsBundle.message("swift.rdt.debug.prepare", code("MasterViewController.swift"), action("GotoFile")))
    }
    caret(11, 9)
    task {
      triggers("ToggleLineBreakpoint", "Debug")
      text(SwiftLessonsBundle.message("swift.rdt.debug.toggle.break", action("ToggleLineBreakpoint"), action("Debug")))
    }
    task {
      triggers("StepInto", "StepOver")
      text(SwiftLessonsBundle.message("swift.rdt.debug.step.into", action("StepInto"), action("StepOver")))
    }
    task {
      triggers("RunToCursor")
      text(SwiftLessonsBundle.message("swift.rdt.debug.run.cursor", action("RunToCursor")))
    }
    task {
      triggers("StepOver")
      text(SwiftLessonsBundle.message("swift.rdt.debug.step.over.next.line"))
    }
    task {
      triggers("EditorSelectWord", "EditorSelectWord", "EvaluateExpression")
      text(SwiftLessonsBundle.message("swift.rdt.debug.eval", code("controllers[controllers.count-1]"), action("EditorSelectWord"), action("EvaluateExpression"), LessonUtil.rawEnter()))
    }
    task {
      triggers("EditorChooseLookupItem")
      text(SwiftLessonsBundle.message("swift.rdt.debug.eval.again", LessonUtil.rawEnter()))
    }
    task {
      triggers("Stop")
      text(SwiftLessonsBundle.message("swift.rdt.debug.stop", action("Stop")))
    }


  }

  override val helpLinks: Map<String, String> get() = mapOf(
    Pair(SwiftLessonsBundle.message("swift.rdt.debug.help.link.1"),
         LessonUtil.getHelpLink("debugging-code.html")),
    Pair(SwiftLessonsBundle.message("swift.rdt.debug.help.link.2"),
         LessonUtil.getHelpLink("debugging-in-appcode.html")),
  )
}