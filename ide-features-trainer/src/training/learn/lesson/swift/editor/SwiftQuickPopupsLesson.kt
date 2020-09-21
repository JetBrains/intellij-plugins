package training.learn.lesson.swift.editor

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftQuickPopupsLesson(module: Module) : KLesson("swift.codeassistance.quickpopups", "Quick Popups", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import Foundation
import UIKit

class Duplicate: UIViewController {

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
    task { caret(18, 34) }
    task {
      triggers("ParameterInfo")
      text("Press ${action("ParameterInfo")} to see the initializer signature.")
    }
    task {
      triggers("EditorEscape")
      text("Press ${action("EditorEscape")} to close the popup.")
    }
    task { caret(4, 26) }
    task {
      triggers("QuickJavaDoc")
      text("Press ${action("QuickJavaDoc")} to see documentation for the symbol at the caret.")
    }
    task { caret(4, 26) }
    task {
      triggers("QuickImplementations")
      text("Press ${action("QuickImplementations")} to see the definition of the symbol at the caret.")
    }
  }
}