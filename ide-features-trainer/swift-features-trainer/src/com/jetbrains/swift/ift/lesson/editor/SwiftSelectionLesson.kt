package com.jetbrains.swift.ift.lesson.editor

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftSelectionLesson : KLesson("swift.editorbasics.selection",
                                     SwiftLessonsBundle.message("swift.editor.selection.name")) {


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
    caret(12, 13)
    task {
      triggers("EditorNextWordWithSelection")
      text(SwiftLessonsBundle.message("swift.editor.selection.word", action("EditorNextWordWithSelection")))
    }
    task {
      triggers("EditorSelectWord")
      text(SwiftLessonsBundle.message("swift.editor.selection.expand", action("EditorSelectWord")))
    }
    task {
      triggers("EditorSelectWord")
      text(SwiftLessonsBundle.message("swift.editor.selection.expand.more", action("EditorSelectWord")))
    }
    task {
      triggers("EditorUnSelectWord")
      text(SwiftLessonsBundle.message("swift.editor.selection.shrink", action("EditorUnSelectWord")))
    }
    task {
      triggers("\$SelectAll")
      text(SwiftLessonsBundle.message("swift.editor.selection.all", action("\$SelectAll")))
    }
  }

  override val helpLinks: Map<String, String> get() = mapOf(
    Pair(SwiftLessonsBundle.message("swift.editor.selection.help.link"),
         LessonUtil.getHelpLink("working-with-source-code.html#editor_code_selection")),
  )
}