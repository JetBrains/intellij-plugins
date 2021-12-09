package com.jetbrains.swift.ift.lesson.rundebugtest

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftRunLesson : KLesson("swift.rdt.run", SwiftLessonsBundle.message("swift.rdt.run.name")) {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class RunExample: UIViewController {

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

    caret(6, 10)
    task {
      triggers("Run")
      text(SwiftLessonsBundle.message("swift.rdt.run.actions", action("Run")))
    }
    task {
      triggers("Stop")
      text(SwiftLessonsBundle.message("swift.rdt.run.stop", action("Stop")))
    }
    task {
      triggers("ChooseRunConfiguration")
      text(SwiftLessonsBundle.message("swift.rdt.run.another", action("ChooseRunConfiguration"), LessonUtil.rawEnter()))
    }
    task {
      triggers("Stop")
      text(SwiftLessonsBundle.message("swift.rdt.run.final", action("Stop")))
    }
  }
}