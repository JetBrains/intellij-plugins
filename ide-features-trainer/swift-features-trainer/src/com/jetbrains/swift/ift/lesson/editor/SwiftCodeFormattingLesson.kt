package com.jetbrains.swift.ift.lesson.editor

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftCodeFormattingLesson : KLesson("swift.codeassistance.codeformatting",
                                          SwiftLessonsBundle.message("swift.editor.format.name"), "Swift") {

  private val sample: LessonSample = parseLessonSample("""

import UIKit

class Format: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()


                        let x = 0

            let y = 50

        let             tableView =     UITableView()

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

    select(10, 1, 15, 1)

    task {
      triggers("ReformatCode")
      text(SwiftLessonsBundle.message("swift.editor.format.reformat", action("ReformatCode")))
    }

    task {
      triggers("ReformatCode")
      text(SwiftLessonsBundle.message("swift.editor.format.reformat.whole.file", action("ReformatCode"), action("EditorEscape"), action("ReformatCode")))
    }
  }
}