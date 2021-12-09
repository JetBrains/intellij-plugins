package com.jetbrains.swift.ift.lesson.editor

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftMoveLesson : KLesson("swift.editorbasics.move", SwiftLessonsBundle.message("swift.editor.move.name")) {


  private val sample: LessonSample = parseLessonSample("""
import Foundation
import UIKit

class Move: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let x = 0
        let y = 50

        let tableView = UITableView()

        header.text = "AppCode"
        header.sizeToFit()
        let header = UILabel()

        self.view.addSubview(tableView)
        tableView.frame = CGRect(x: x, y: y, width: 320, height: 400)
        tableView.tableHeaderView = header
    }

}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    caret(18, 9)
    task {
      triggers("MoveLineDown", "MoveLineDown")
      text(SwiftLessonsBundle.message("swift.editor.move.line.down", action("MoveLineDown"), code("viewDidLoad")))
    }
    caret(16, 9)
    task {
      triggers("MoveLineUp", "MoveLineUp")
      text(SwiftLessonsBundle.message("swift.editor.move.line.up", action("MoveLineUp"), code("header")))
    }
  }
}