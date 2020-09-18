package training.learn.lesson.swift.editor

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftCodeFormattingLesson(module: Module) : KLesson("swift.codeassistance.codeformatting", "Code Formatting", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""

import UIKit

class Format: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()


                        let x = 0

            let y = 50

        let             tableView =     UITableView()

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

    task { select(10, 1, 15, 1) }

    task {
      triggers("ReformatCode")
      text("<ide/> can help you correct code formatting with just one action. Try reformatting the selected code with ${
        action("ReformatCode")
      }.")
    }

    task {
      triggers("ReformatCode")
      text("To reformat the whole source file, use ${action("ReformatCode")} when no lines are selected. Press ${
        action("EditorEscape")
      } and then ${action("ReformatCode")}.")
    }


  }
}