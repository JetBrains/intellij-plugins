package training.learn.lesson.swift.refactorings

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftRenameLesson(module: Module) : KLesson("swift.refactorings.rename", "Rename", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class Rename: UIViewController {

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

    task { text("You can rename anything by placing the caret on a symbol and using the Rename refactoring.") }
    task {
      triggers("GotoFile", "MasterViewController.swift")
      text("Navigate to ${code("MasterViewController.swift")} by pressing ${action("GotoFile")}")
    }
    task { caret(6, 10) }
    task {
      triggers("RenameElement", "NextTemplateVariable")
      text("Let's start with something simple like renaming the ${code("objects")} field to ${code("array")}. Press ${
          action("RenameElement")
      }, enter a new name, and then press ${LessonUtil.rawEnter()}")
    }
    task { caret(5, 39) }
    task {
      triggers("RenameElement", "NextTemplateVariable")
      text("Now, repeat the same actions and rename the ${code("DetailViewController")} type to something new.")
    }
    task {
      triggers("FindUsages")
      text("Press ${
          action("FindUsages")
      }. As you can see, the rename works globally, even renaming occurrences in .xib and .storyboard files.")
    }


  }
}