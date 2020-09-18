package training.learn.lesson.swift.refactorings

import com.intellij.icons.AllIcons
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftChangeSignatureLesson(module: Module) : KLesson("swift.refactorings.change.signature", "Change Signature", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class ChangeSignature: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let x = 0
        let y = 50

        let tableView = UITableView()
        method(tableView: tableView, x: x, y: y)
        self.view.addSubview(tableView)
    }

    private func method(tableView: UITableView, x: Int, y: Int) {
        let header = UILabel()
        header.text = "AppCode"
        header.sizeToFit()

        tableView.frame = CGRect(x: x, y: y, width: 320, height: 400)
        tableView.tableHeaderView = header
    }
}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    task { caret(16, 22) }
    task {
      text(
        "<strong>Change Signature</strong> is a refactoring that lets you change the names of methods and functions, edit internal and external parameter names, change their order, and change the visibility of methods and functions – all at once.")
    }
    task {
      triggers("ChangeSignature")
      text("Press ${action("ChangeSignature")} to change the ${code("method")} name to ${code("setup")}.")
    }
    task {
      triggers("ChangeSignature")
      text("Press ${action("ChangeSignature")} again. Click the row with the ${code("tableView")} parameter and change it to ${
        code("table")
      }, or add an internal parameter name.")
    }
    task {
      triggers("ChangeSignature")
      text("Finally, invoke <strong>Change Signature</strong> again to change the parameter order using ${
        icon(AllIcons.General.ArrowUp)
      }/${icon(AllIcons.General.ArrowDown)} buttons or <shortcut>⌥↑</shortcut>/<shortcut>⌥↓</shortcut> shortcuts.")
    }


  }
}