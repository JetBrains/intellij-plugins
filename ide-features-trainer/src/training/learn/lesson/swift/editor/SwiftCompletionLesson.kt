package training.learn.lesson.swift.editor

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftCompletionLesson(module: Module) : KLesson("swift.completions.basiccompletion", "Completion", module, "Swift") {

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
      text("By default, <ide/> completes your code instantly. Start typing ${
          code(".stf")
      } right where the caret is, and press ${LessonUtil.rawEnter()} to select the ${code("sizeToFit()")} function.")
    }
    task { caret(15, 20) }
    task { text("To activate Basic Completion, press ${action("CodeCompletion")}. The lookup menu will display again.") }
    task { caret(19, 35) }
    task { type(" ") }

    task {
      triggers("SmartTypeCompletion", "EditorChooseLookupItem")
      text(
        "Smart Type Completion filters the list of suggestions to include only those types that are applicable in the current context. Press ${
            action("SmartTypeCompletion")
        } to see the list of matching suggestions. Choose the first one by pressing ${LessonUtil.rawEnter()}.")
    }
    task { caret(19, 37) }
    task { type("s") }

    task {
      triggers("CodeCompletion")
      text("Now press ${action("CodeCompletion")} again to show completion options.")
    }
    task {
      triggers("EditorChooseLookupItemReplace")
      text("Select the ${code("singleLine")} item and press ${action("EditorChooseLookupItemReplace")} (instead of ${
          action("EditorChooseLookupItem")
      }). This overwrites the word at the caret rather than simply inserting it.")
    }
    task { caret(22, 21) }
    task {
      triggers("CodeCompletion", "EditorChooseLookupItemReplace")
      text("Now invoke ${action("CodeCompletion")} and ${action("EditorChooseLookupItemReplace")} to easily overwrite ${
          code("animate(withDuration:animations:completion:)")
      } with ${code("animate(withDuration:animations:)")}")
    }
    task { text("Awesome! Click the button below to start the next lesson, or use ${action("learn.next.lesson")}.") }


  }
}