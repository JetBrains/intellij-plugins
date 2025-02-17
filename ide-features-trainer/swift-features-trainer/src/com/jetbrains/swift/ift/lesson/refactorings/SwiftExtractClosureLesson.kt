package com.jetbrains.swift.ift.lesson.refactorings

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftExtractClosureLesson : KLesson("swift.refactorings.extract.closure",
                                          SwiftLessonsBundle.message("swift.refactoring.extract.closure.name")) {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class ExtractClosure: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let x = 0
        let y = 50

        var tableView = UITableView()

        var header = UILabel()
        header.text = "AppCode"
        header.sizeToFit()

        tableView.frame = CGRect(x: x, y: y, width: 320, height: 400)
        tableView.tableHeaderView = header
        self.view.addSubview(tableView)
    }
}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    select(14, 1, 19, 1)

    text(SwiftLessonsBundle.message("swift.refactoring.extract.closure.intro"))
    task {
      @Suppress("InjectedReferences", "UnresolvedPluginConfigReference")
      triggers("Refactorings.QuickListPopupAction", "SwiftIntroduceClosureVariable")
      text(SwiftLessonsBundle.message("swift.refactoring.extract.closure.exec", action("Refactorings.QuickListPopupAction")))
    }
  }

  override val helpLinks: Map<String, String> get() = mapOf(
    Pair(SwiftLessonsBundle.message("swift.refactoring.extract.closure.help.link"),
         LessonUtil.getHelpLink("extract-closure.html")),
  )
}