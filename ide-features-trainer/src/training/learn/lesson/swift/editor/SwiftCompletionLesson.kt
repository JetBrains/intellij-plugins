package training.learn.lesson.swift.editor

import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftCompletionLesson(module: Module) : KLesson("swift.completions.basiccompletion", LessonsBundle.message("swift.editor.completion.name"), module, "Swift") {

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

    task { caret(15, 16) }
    task {
      triggers("EditorChooseLookupItem")
      text(LessonsBundle.message("swift.editor.completion.basic", code(".stf"), LessonUtil.rawEnter(), code("sizeToFit()")))
    }
    task { caret(15, 20) }
    task { text(LessonsBundle.message("swift.editor.completion.basic.shortcut", action("CodeCompletion"))) }
    task { caret(19, 35) }
    task { type(" ") }

    task {
      triggers("SmartTypeCompletion", "EditorChooseLookupItem")
      text(LessonsBundle.message("swift.editor.completion.smart", action("SmartTypeCompletion"), LessonUtil.rawEnter()))
    }
    task { caret(19, 37) }
    task { type("s") }

    task {
      triggers("CodeCompletion")
      text(LessonsBundle.message("swift.editor.completion.show", action("CodeCompletion")))
    }
    task {
      triggers("EditorChooseLookupItemReplace")
      text(LessonsBundle.message("swift.editor.completion.tab", code("singleLine"), action("EditorChooseLookupItemReplace"), action("EditorChooseLookupItem")))
    }
    task { caret(22, 21) }
    task {
      triggers("CodeCompletion", "EditorChooseLookupItemReplace")
      text(LessonsBundle.message("swift.editor.completion.tab.method", action("CodeCompletion"), action("EditorChooseLookupItemReplace"), code("animate(withDuration:animations:completion:)"), code("animate(withDuration:animations:)")))
    }
    task { text(LessonsBundle.message("swift.editor.completion.go.next", action("learn.next.lesson"))) }
  }
}