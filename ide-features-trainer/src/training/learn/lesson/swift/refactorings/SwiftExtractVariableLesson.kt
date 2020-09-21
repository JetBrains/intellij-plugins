package training.learn.lesson.swift.refactorings

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftExtractVariableLesson(module: Module) : KLesson("swift.refactorings.extract.variable", "Extract Variable", module, "Swift") {

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
    task { caret(8, 61) }
    task {
      triggers("IntroduceVariable", "NextTemplateVariable")
      text("Press ${action("IntroduceVariable")} to extract the constant ${code("50")} to the variable or field ${code("y")}.")
    }
    task {
      text("Extract Variable does not require you to select the exact code you want to extract. You can simply place the caret on the statement you want to extract and then select the needed expression.")
    }
    task {
      triggers("IntroduceVariable", "NextTemplateVariable")
      text("Now, press ${action("IntroduceVariable")} and extract ${code("CGRect")} to the variable ${code("frame")}, by selecting it from the list.")
    }
  }
}