package training.learn.lesson.swift.rundebugtest

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftRunLesson(module: Module) : KLesson("swift.rdt.run", "Run", module, "Swift") {

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
      text("<ide/> has two different actions for running the project and for debugging it (because in general, debugging is slower). Try running your application by pressing ${action("Run")}.")
    }
    task {
      triggers("Stop")
      text("Stop your application by pressing ${action("Stop")}.")
    }
    task {
      triggers("ChooseRunConfiguration")
      text("Great! To select another Run Configuration or simulator for the current run configuration and quickly run your application, press ${action("ChooseRunConfiguration")}, select needed Run Configuration or simulator, and then press ${LessonUtil.rawEnter()}.")
    }
    task {
      triggers("Stop")
      text("Stop your application by pressing ${action("Stop")}.")
    }
  }
}