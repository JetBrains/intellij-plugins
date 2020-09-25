package training.learn.lesson.swift.editor

import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftSelectionLesson(module: Module) : KLesson("swift.editorbasics.selection", LessonsBundle.message("swift.editor.selection.name"), module, "Swift") {


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
      text(LessonsBundle.message("swift.editor.selection.word", action("EditorNextWordWithSelection")))
    }
    task {
      triggers("EditorSelectWord")
      text(LessonsBundle.message("swift.editor.selection.expand", action("EditorSelectWord")))
    }
    task {
      triggers("EditorSelectWord")
      text(LessonsBundle.message("swift.editor.selection.expand.more", action("EditorSelectWord")))
    }
    task {
      triggers("EditorUnSelectWord")
      text(LessonsBundle.message("swift.editor.selection.shrink", action("EditorUnSelectWord")))
    }
    task {
      triggers("\$SelectAll")
      text(LessonsBundle.message("swift.editor.selection.all", action("\$SelectAll")))
    }
  }
}