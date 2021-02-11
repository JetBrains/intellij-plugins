package com.jetbrains.swift.ift.lesson.refactorings

import com.intellij.icons.AllIcons
import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftExtractMethodLesson : KLesson("swift.refactorings.extract.method",
                                         SwiftLessonsBundle.message("swift.refactoring.extract.method.name"), "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class ExtractMethod: UIViewController {

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

    select(11, 9, 18, 43)
    task {
      triggers("ExtractMethod")
      text(SwiftLessonsBundle.message("swift.refactoring.extract.method.intro", code("setup"), action("ExtractMethod")))
    }
    task {
      triggers("\$Undo")
      text(SwiftLessonsBundle.message("swift.refactoring.extract.method.undo", action("\$Undo")))
    }
    select(11, 9, 18, 43)

    text(SwiftLessonsBundle.message("swift.refactoring.extract.method.change.params", icon(AllIcons.General.ArrowUp),
                               icon(AllIcons.General.ArrowDown)))
    task {
      triggers("ExtractMethod")
      text(SwiftLessonsBundle.message("swift.refactoring.extract.method.exec.again", action("ExtractMethod")))
    }
  }
}