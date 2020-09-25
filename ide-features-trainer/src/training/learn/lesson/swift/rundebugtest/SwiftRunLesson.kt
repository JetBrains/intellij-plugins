package training.learn.lesson.swift.rundebugtest

import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftRunLesson(module: Module) : KLesson("swift.rdt.run", LessonsBundle.message("swift.rdt.run.name"), module, "Swift") {

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

    task { caret(6, 10) }
    task {
      triggers("Run")
      text(LessonsBundle.message("swift.rdt.run.actions", action("Run")))
    }
    task {
      triggers("Stop")
      text(LessonsBundle.message("swift.rdt.run.stop", action("Stop")))
    }
    task {
      triggers("ChooseRunConfiguration")
      text(LessonsBundle.message("swift.rdt.run.another", action("ChooseRunConfiguration"), LessonUtil.rawEnter()))
    }
    task {
      triggers("Stop")
      text(LessonsBundle.message("swift.rdt.run.final", action("Stop")))
    }
  }
}