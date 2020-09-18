package training.learn.lesson.swift.codegeneration

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftOverrideImplementLesson(module: Module) : KLesson("swift.codegeneration.overrideimplement", "Override / Implement", module,
                                                             "Swift") {

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

    task { caret(22, 5) }
    task {
      text(
        "You can override any method of a parent class or implement any protocols, by using the <strong>Override/Implement</strong> actions (${
            action("OverrideMethods")
        }/${action("ImplementMethods")})")
    }
    task {
      triggers("ImplementMethods")
      text("Press ${action("ImplementMethods")} → ${LessonUtil.rawEnter()} and add stubs for all required methods from ${
          code("UITableViewDataSource")
      }")
    }
    task { caret(22, 5) }
    task {
      triggers("OverrideMethods")
      text("Now let's try overriding several methods at once. Press ${action("OverrideMethods")} and start typing ${
          code("viewAppear")
      }. The list of methods and properties you can override should be filtered down to just two methods. Press ${
          action("\$SelectAll")
      }→ ${LessonUtil.rawEnter()} to override them.")
    }


  }
}