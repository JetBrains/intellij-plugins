package training.learn.lesson.swift.refactorings

import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftRenameLesson(module: Module) : KLesson("swift.refactorings.rename", LessonsBundle.message("swift.refactoring.rename.name"), module, "Swift") {

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

    text(LessonsBundle.message("swift.refactoring.rename.any"))
    task {
      triggers("GotoFile", "MasterViewController.swift")
      text(LessonsBundle.message("swift.refactoring.rename.go.to.file", code("MasterViewController.swift"), action("GotoFile")))
    }
    caret(6, 10)
    task {
      triggers("RenameElement", "NextTemplateVariable")
      text(LessonsBundle.message("swift.refactoring.rename.var", code("objects"), code("array"), action("RenameElement"), LessonUtil.rawEnter()))
    }
    caret(5, 39)
    task {
      triggers("RenameElement", "NextTemplateVariable")
      text(LessonsBundle.message("swift.refactoring.rename.class", code("DetailViewController")))
    }
    task {
      triggers("FindUsages")
      text(LessonsBundle.message("swift.refactoring.rename.check", action("FindUsages")))
    }
  }
}