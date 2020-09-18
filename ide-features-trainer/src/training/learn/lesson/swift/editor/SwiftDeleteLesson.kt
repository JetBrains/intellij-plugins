package training.learn.lesson.swift.editor

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftDeleteLesson(module: Module) : KLesson("swift.editorbasics.deleteline", "Delete", module, "Swift") {


  private val sample: LessonSample = parseLessonSample("""
import Foundation
import UIKit

class Delete: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let x = 0
        let y = 50

        let tableView = UITableView(frame: CGRect.zero)

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


    task { caret(12, 9) }
    task {
      triggers("EditorDeleteLine")
      text("Delete the current line with ${action("EditorDeleteLine")}.")
    }
    task {
      triggers("\$Undo")
      text("To restore the deleted line, press ${action("\$Undo")}.")
    }


  }
}