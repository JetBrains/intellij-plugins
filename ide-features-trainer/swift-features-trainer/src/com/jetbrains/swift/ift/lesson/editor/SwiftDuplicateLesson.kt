package com.jetbrains.swift.ift.lesson.editor

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftDuplicateLesson : KLesson("swift.editorbasics.duplicate",
                                     SwiftLessonsBundle.message("swift.editor.duplicate.name"), "Swift") {


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

    caret(15, 22)
    task {
      triggers("EditorDuplicate")
      text(SwiftLessonsBundle.message("swift.editor.duplicate.line", action("EditorDuplicate")))
    }
    select(14, 1, 18, 1)

    task {
      triggers("EditorDuplicate")
      text(SwiftLessonsBundle.message("swift.editor.duplicate.several.lines", action("EditorDuplicate")))
    }
  }
}