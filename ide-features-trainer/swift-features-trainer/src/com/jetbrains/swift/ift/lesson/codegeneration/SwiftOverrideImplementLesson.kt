package com.jetbrains.swift.ift.lesson.codegeneration

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftOverrideImplementLesson : KLesson("swift.codegeneration.overrideimplement",
                                             SwiftLessonsBundle.message("swift.codegeneration.overrideimplement.name")) {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class OverrideImplement: UIViewController, UITableViewDataSource {

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
        tableView.dataSource = self
        self.view.addSubview(tableView)
    }

}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    caret(22, 5)
    text(SwiftLessonsBundle.message("swift.codegeneration.overrideimplement.intro", action("OverrideMethods"), action("ImplementMethods")))
    task {
      triggers("ImplementMethods")
      text(SwiftLessonsBundle.message("swift.codegeneration.overrideimplement.implement", action("ImplementMethods"), LessonUtil.rawEnter(),
                                 code("UITableViewDataSource")))
    }
    caret(22, 5)
    task {
      triggers("OverrideMethods")
      text(SwiftLessonsBundle.message("swift.codegeneration.overrideimplement.override", action("OverrideMethods"), code("viewAppear"), action("\$SelectAll"), LessonUtil.rawEnter()))
    }


  }

  override val suitableTips = listOf("OverrideImplementMethods")

  override val helpLinks: Map<String, String> get() = mapOf(
    Pair(SwiftLessonsBundle.message("swift.codegeneration.overrideimplement.help.link.1"),
         LessonUtil.getHelpLink("generating-code.html#override_method")),
    Pair(SwiftLessonsBundle.message("swift.codegeneration.overrideimplement.help.link.2"),
         LessonUtil.getHelpLink("generating-code.html#implement_methods")),
  )
}