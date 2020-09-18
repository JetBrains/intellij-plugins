package training.learn.lesson.swift.refactorings

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftExtractClosureLesson(module: Module) : KLesson("swift.refactorings.extract.closure", "Extract Closure", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class ExtractClosure: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let x = 0
        let y = 50

        var tableView = UITableView()

        var header = UILabel()
        header.text = "AppCode"
        header.sizeToFit()

        tableView.frame = CGRect(x: x, y: y, width: 320, height: 400)
        tableView.tableHeaderView = header
        self.view.addSubview(tableView)
    }
}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    task { select(14, 1, 19, 1) }

    task {
      text(
        "Extract Closure allows you to encapsulate a code selection into a new closure, with customizable parameters and return type. It works similarly to the Extract Method refactoring.")
    }
    task {
      triggers("Refactorings.QuickListPopupAction", "SwiftIntroduceClosureVariable")
      text("Press ${action("Refactorings.QuickListPopupAction")} and then select <strong>Closure...</strong>.")
    }


  }
}