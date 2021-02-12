package com.jetbrains.swift.ift.lesson.refactorings

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftExtractVariableLesson : KLesson("swift.refactorings.extract.variable",
                                           SwiftLessonsBundle.message("swift.refactoring.extract.variable.name")) {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class ExtractVariable: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let tableView = UITableView(frame: CGRect(x: 0, y: 50, width: 320, height: 400))

        let header = UILabel()

        header.text = "AppCode"
        header.frame = CGRect(x: 0, y: 50, width: 320, height: 400)
        header.sizeToFit()

        tableView.tableHeaderView = header

        self.view.addSubview(tableView)
    }
}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    caret(8, 61)
    task {
      triggers("IntroduceVariable", "NextTemplateVariable")
      text(SwiftLessonsBundle.message("swift.refactoring.extract.variable.exec", action("IntroduceVariable"), code("50"), code("y")))
    }
    text(SwiftLessonsBundle.message("swift.refactoring.extract.variable.description"))
    task {
      triggers("IntroduceVariable", "NextTemplateVariable")
      text(
        SwiftLessonsBundle.message("swift.refactoring.extract.variable.exec.again", action("IntroduceVariable"), code("CGRect"), code("frame")))
    }
  }
}