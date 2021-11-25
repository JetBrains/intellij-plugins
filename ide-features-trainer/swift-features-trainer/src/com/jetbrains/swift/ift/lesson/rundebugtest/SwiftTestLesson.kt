package com.jetbrains.swift.ift.lesson.rundebugtest

import com.intellij.icons.AllIcons
import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftTestLesson : KLesson("swift.rdt.test", SwiftLessonsBundle.message("swift.rdt.test.name")) {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class TestExample: UIViewController {

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

    task {
      triggers("GotoFile", "LearnProjectTests.swift")
      text(SwiftLessonsBundle.message("swift.rdt.test.prepare", code("LearnProjectTests.swift"), action("GotoFile")))
    }
    text(SwiftLessonsBundle.message("swift.rdt.test.intro"))
    text(SwiftLessonsBundle.message("swift.rdt.test.intro.tests"))
    caret(5, 5)
    task {
      triggers("RunClass")
      text(SwiftLessonsBundle.message("swift.rdt.test.suite", action("RunClass")))
    }
    task {
      triggers("com.intellij.util.config.DumbAwareToggleInvertedBooleanProperty")
      text(SwiftLessonsBundle.message("swift.rdt.test.show", icon(AllIcons.RunConfigurations.ShowPassed)))
    }
    caret(17, 19)
    task {
      triggers("RunClass")
      text(SwiftLessonsBundle.message("swift.rdt.test.single", action("RunClass")))
    }
    caret(13, 9)
    task {
      triggers("ToggleLineBreakpoint", "DebugClass")
      text(SwiftLessonsBundle.message("swift.rdt.test.debug.single", action("ToggleLineBreakpoint"), action("DebugClass")))
    }
    task {
      triggers("Resume")
      text(SwiftLessonsBundle.message("swift.rdt.test.resume", action("Resume")))
    }
    task {
      triggers("Stop")
      text(SwiftLessonsBundle.message("swift.rdt.test.stop.debug", action("Stop")))
    }
    task {
      triggers("GotoFile", "LearnProjectTests.swift")
      text(SwiftLessonsBundle.message("swift.rdt.test.go.back", code("LearnProjectTests.swift"), action("GotoFile")))
    }
    caret(18, 97)
    task { type("\n") }
    caret(19, 9)
    task { type("\t\tXCTAssert(false)") }
    caret(5, 5)
    task {
      triggers("RunClass")
      text(SwiftLessonsBundle.message("swift.rdt.test.failing", action("RunClass")))
    }
    task {
      triggers("com.intellij.util.config.DumbAwareToggleInvertedBooleanProperty")
      text(SwiftLessonsBundle.message("swift.rdt.test.filter.failed", icon(AllIcons.RunConfigurations.ShowPassed)))
    }
    caret(19, 21)
    task {
      triggers("EditorDeleteLine")
      text(SwiftLessonsBundle.message("swift.rdt.test.delete.failed", code("XCTAssert(false)"), action("EditorDeleteLine")))
    }
    task {
      triggers("com.jetbrains.cidr.execution.testing.unit.AppCodeOCUnitRerunFailedTestsAction")
      text(SwiftLessonsBundle.message("swift.rdt.test.rerun.failed", icon(AllIcons.RunConfigurations.RerunFailedTests)))
    }
    caret(5, 5)
    task {
      triggers("RunClass", "com.intellij.execution.testframework.ToolbarPanel\$SortByDurationAction")
      text(SwiftLessonsBundle.message("swift.rdt.test.additional", icon(AllIcons.RunConfigurations.SortbyDuration), icon(AllIcons.ObjectBrowser.Sorted), action("RunClass")))
    }
    task {
      triggers("com.intellij.execution.testframework.sm.runner.history.actions.ImportTestsFromHistoryAction")
      text(SwiftLessonsBundle.message("swift.rdt.test.history", icon(AllIcons.Vcs.History)))
    }
    text(SwiftLessonsBundle.message("swift.rdt.test.more"))
  }

  override val suitableTips = listOf("RunTests")

  override val helpLinks: Map<String, String> get() = mapOf(
    Pair(SwiftLessonsBundle.message("swift.rdt.test.help.link.1"),
         LessonUtil.getHelpLink("create-tests.html")),
    Pair(SwiftLessonsBundle.message("swift.rdt.test.help.link.2"),
         LessonUtil.getHelpLink("unit-testing-in-appcode.html")),
  )
}