package training.learn.lesson.swift.editor

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftSelectionLesson(module: Module) : KLesson("swift.editorbasics.selection", "Selection", module, "Swift") {


  private val sample: LessonSample = parseLessonSample("""
import Foundation
import UIKit

class Select: UIViewController {

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


    task { caret(12, 13) }
    task {
      triggers("EditorNextWordWithSelection")
      text("Place the caret before any word. Press ${
          action("EditorNextWordWithSelection")
      } to move the caret to the next word and select everything in between.")
    }
    task {
      triggers("EditorSelectWord")
      text("Press ${action("EditorSelectWord")} to extend the selection to the next code block.")
    }
    task {
      triggers("EditorSelectWord")
      text("Try increasing your selection with ${action("EditorSelectWord")} until your whole file is selected.")
    }
    task {
      triggers("EditorUnSelectWord")
      text("${action("EditorUnSelectWord")} shrinks the current selection. Try pressing it.")
    }
    task {
      triggers("\$SelectAll")
      text("Now select the whole file instantly with ${action("\$SelectAll")}.")
    }


  }
}