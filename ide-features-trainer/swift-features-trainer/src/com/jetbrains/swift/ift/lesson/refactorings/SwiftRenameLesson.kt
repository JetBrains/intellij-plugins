package com.jetbrains.swift.ift.lesson.refactorings

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.learn.lesson.kimpl.*

class SwiftRenameLesson : KLesson("swift.refactorings.rename", SwiftLessonsBundle.message("swift.refactoring.rename.name"),
                                  "Swift") {

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

    text(SwiftLessonsBundle.message("swift.refactoring.rename.any"))
    task {
      triggers("GotoFile", "MasterViewController.swift")
      text(SwiftLessonsBundle.message("swift.refactoring.rename.go.to.file", code("MasterViewController.swift"), action("GotoFile")))
    }
    caret(6, 10)
    task {
      triggers("RenameElement", "NextTemplateVariable")
      text(SwiftLessonsBundle.message("swift.refactoring.rename.var", code("objects"), code("array"), action("RenameElement"), LessonUtil.rawEnter()))
    }
    caret(5, 39)
    task {
      triggers("RenameElement", "NextTemplateVariable")
      text(SwiftLessonsBundle.message("swift.refactoring.rename.class", code("DetailViewController")))
    }
    task {
      triggers("FindUsages")
      text(SwiftLessonsBundle.message("swift.refactoring.rename.check", action("FindUsages")))
    }
  }
}