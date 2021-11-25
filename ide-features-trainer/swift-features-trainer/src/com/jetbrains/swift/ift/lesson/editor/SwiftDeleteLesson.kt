package com.jetbrains.swift.ift.lesson.editor

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftDeleteLesson : KLesson("swift.editorbasics.deleteline", SwiftLessonsBundle.message("swift.editor.delete.name")) {


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

    caret(12, 9)
    task {
      triggers("EditorDeleteLine")
      text(SwiftLessonsBundle.message("swift.editor.delete.action", action("EditorDeleteLine")))
    }
    task {
      triggers("\$Undo")
      text(SwiftLessonsBundle.message("swift.editor.delete.undo", action("\$Undo")))
    }
  }

  override val suitableTips = listOf("DeleteLine")

  override val helpLinks: Map<String, String> get() = mapOf(
    Pair(SwiftLessonsBundle.message("swift.editor.delete.help.link"),
         LessonUtil.getHelpLink("working-with-source-code.html#editor_lines_code_blocks")),
  )
}