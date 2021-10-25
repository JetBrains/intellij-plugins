package com.jetbrains.swift.ift.lesson.editor

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftCompletionLesson : KLesson("swift.completions.basiccompletion",
                                      SwiftLessonsBundle.message("swift.editor.completion.name")) {

  private val sample: LessonSample = parseLessonSample("""
import Foundation
import UIKit
class Completion: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let x = 0
        let y = 50

        let tableView = UITableView()

        let header = UILabel()
        header.text = "AppCode"
        header.

        tableView.frame = CGRect(x: x, y: y, width: 320, height: 400)
        tableView.tableHeaderView = header
        tableView.separatorStyle =
        self.view.addSubview(tableView)

        UIView.animate(withDuration: 1.0, animations: { tableView.backgroundColor = .brown }, completion: nil)
    }
}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    caret(15, 16)
    task {
      triggers("EditorChooseLookupItem")
      text(SwiftLessonsBundle.message("swift.editor.completion.basic", code("stf"), LessonUtil.rawEnter(), code("sizeToFit()")))
    }
    caret(15, 20)
    text(SwiftLessonsBundle.message("swift.editor.completion.basic.shortcut", action("CodeCompletion")))
    caret(19, 35)
    task { type(" ") }

    task {
      triggers("SmartTypeCompletion", "EditorChooseLookupItem")
      text(SwiftLessonsBundle.message("swift.editor.completion.smart", action("SmartTypeCompletion"), LessonUtil.rawEnter()))
    }
    caret(19, 37)
    task { type("s") }

    task {
      triggers("CodeCompletion")
      text(SwiftLessonsBundle.message("swift.editor.completion.show", action("CodeCompletion")))
    }
    task {
      triggers("EditorChooseLookupItemReplace")
      text(SwiftLessonsBundle.message("swift.editor.completion.tab", code("singleLine"), action("EditorChooseLookupItemReplace"), action("EditorChooseLookupItem")))
    }
    caret(22, 21)
    task {
      triggers("CodeCompletion", "EditorChooseLookupItemReplace")
      text(SwiftLessonsBundle.message("swift.editor.completion.tab.method", action("CodeCompletion"), action("EditorChooseLookupItemReplace"),
                                 code("animate(withDuration:animations:completion:)"), code("animate(withDuration:animations:)")))
    }
    text(SwiftLessonsBundle.message("swift.editor.completion.go.next", LessonUtil.rawEnter()))
  }
}