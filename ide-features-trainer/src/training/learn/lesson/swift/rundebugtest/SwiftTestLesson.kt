package training.learn.lesson.swift.rundebugtest

import com.intellij.icons.AllIcons
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftTestLesson(module: Module) : KLesson("swift.rdt.test", "Test", module, "Swift") {

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
      text("Navigate to ${code("LearnProjectTests.swift")} by using ${action("GotoFile")}.")
    }
    task {
      text(
        "<ide/> supports many testing frameworks including XCTest, Quick, Kiwi, Catch, Boost.Test, and Google Test. Let's take a look at all the common features you can use when testing your application.")
    }
    task {
      text(
        "<ide/> automatically identifies the test scope based on the caret position. When caret is somewhere in the test file, all tests in this file can be executed. The same applies to a single test - to run a single test, just place the caret inside the test method.")
    }
    task { caret(5, 5) }
    task {
      triggers("RunClass")
      text("Press ${action("RunClass")} to run all tests in the test suite.")
    }
    task {
      triggers("com.intellij.util.config.DumbAwareToggleInvertedBooleanProperty")
      text("Now let's show tests in the tree view. Click the ${icon(AllIcons.RunConfigurations.ShowPassed)} icon.")
    }
    task { caret(17, 19) }
    task {
      triggers("RunClass")
      text("Use the same ${action("RunClass")} shortcut to run a single test.")
    }
    task { caret(13, 9) }
    task {
      triggers("ToggleLineBreakpoint", "DebugClass")
      text("Now let's debug our test. Toggle the line breakpoint by using ${
        action("ToggleLineBreakpoint")
      } and then debug the test with ${action("DebugClass")}.")
    }
    task {
      triggers("Resume")
      text("Press ${action("Resume")} to continue execution.")
    }
    task {
      triggers("Stop")
      text("Press ${action("Stop")} to stop debugging.")
    }
    task {
      triggers("GotoFile", "LearnProjectTests.swift")
      text("Navigate back to ${code("LearnProjectTests.swift")} by pressing ${action("GotoFile")}.")
    }
    task { caret(18, 97) }
    task { type("\n") }
    task { caret(19, 9) }
    task { type("\t\tXCTAssert(false)") }
    task { caret(5, 5) }
    task {
      triggers("RunClass")
      text("Let's now have a failing test. Run all tests again with the ${action("RunClass")} shortcut.")
    }
    task {
      triggers("com.intellij.util.config.DumbAwareToggleInvertedBooleanProperty")
      text("Filter out only failed tests by clicking the ${icon(AllIcons.RunConfigurations.ShowPassed)} icon.")
    }
    task { caret(19, 21) }
    task {
      triggers("EditorDeleteLine")
      text("Delete ${code("XCTAssert(false)")} by using ${action("EditorDeleteLine")}.")
    }
    task {
      triggers("com.jetbrains.cidr.execution.testing.unit.AppCodeOCUnitRerunFailedTestsAction")
      text("Now, rerun only failed tests by clicking ${icon(AllIcons.RunConfigurations.RerunFailedTests)}.")
    }
    task { caret(5, 5) }
    task {
      triggers("RunClass", "com.intellij.execution.testframework.ToolbarPanel\$SortByDurationAction")
      text("Great! Additional controls in the <strong>Test Runner</strong> toolwindow allow you to sort tests by duration (${
        icon(AllIcons.RunConfigurations.SortbyDuration)
      }) or alphabetically (${icon(AllIcons.ObjectBrowser.Sorted)}). Run all the tests again by pressing ${
        action("RunClass")
      } and then try sorting them by duration.")
    }
    task {
      triggers("com.intellij.execution.testframework.sm.runner.history.actions.ImportTestsFromHistoryAction")
      text(
        "Built-in test history automatically saves the results of several most recent rest runs and can also load them in the test runner toolwindow. Try loading one of the previous test runs by clicking the ${
          icon(AllIcons.Vcs.History)
        } icon and selecting one of the items in the list.")
    }
    task {
      text(
        "That's it! For some of the frameworks, such as <strong>Quick</strong>, <ide/> offers even more enhanced rendering of test results. For an overview of all test frameworks supported in <ide/>, watch <a href=\"https://www.youtube.com/watch?v=DXvx6xNG_jc\">this video</a>.")
    }


  }
}