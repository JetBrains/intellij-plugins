package training.learn.lesson.swift.rundebugtest

import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftDebugLesson(module: Module) : KLesson("swift.rdt.debug", LessonsBundle.message("swift.rdt.debug.name"), module, "Swift") {

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

    task { text(LessonsBundle.message("swift.rdt.debug.intro")) }
    task {
      triggers("GotoFile", "MasterViewController.swift")
      text(LessonsBundle.message("swift.rdt.debug.prepare", code("MasterViewController.swift"), action("GotoFile")))
    }
    task { caret(11, 9) }
    task {
      triggers("ToggleLineBreakpoint", "Debug")
      text(LessonsBundle.message("swift.rdt.debug.toggle.break", action("ToggleLineBreakpoint"), action("Debug")))
    }
    task {
      triggers("StepInto", "StepOver")
      text(LessonsBundle.message("swift.rdt.debug.step.into", action("StepInto"), action("StepOver")))
    }
    task {
      triggers("RunToCursor")
      text(LessonsBundle.message("swift.rdt.debug.run.cursor", action("RunToCursor")))
    }
    task {
      triggers("StepOver")
      text(LessonsBundle.message("swift.rdt.debug.step.over.next.line"))
    }
    task {
      triggers("EditorSelectWord", "EditorSelectWord", "EvaluateExpression")
      text(LessonsBundle.message("swift.rdt.debug.eval", code("controllers[controllers.count-1]"), action("EditorSelectWord"), action("EvaluateExpression"), LessonUtil.rawEnter()))
    }
    task {
      triggers("EditorChooseLookupItem")
      text(LessonsBundle.message("swift.rdt.debug.eval.again", LessonUtil.rawEnter()))
    }
    task {
      triggers("Stop")
      text(LessonsBundle.message("swift.rdt.debug.stop", action("Stop")))
    }


  }
}