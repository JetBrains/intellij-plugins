package training.learn.lesson.swift.navigation

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftPreciseNavigationLesson(module: Module) : KLesson("swift.navigation.precise", "Precise Navigation", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class PreciseNavigationController : UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()

        let unused = 1


        let anotherUnused = 2
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        let error = 1 / 0
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        let error = 1 / 0
    }
}

""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    task { caret(5, 19) }
    task {
      text("Several shortcuts for navigating within a code file can make your development much more efficient. Let's take a quick look at these.")
    }
    task {
      triggers("GotoNextError")
      text("There are several warnings and an error in this file. Navigate to the first error by using ${action("GotoNextError")}.")
    }
    task {
      triggers("GotoNextError")
      text("Now, jump to the next error by using the same shortcut again.")
    }
    task {
      triggers("GotoPreviousError")
      text("Jump back to the first error with ${action("GotoPreviousError")}.")
    }
    task {
      triggers("CommentByLineComment", "GotoNextError", "CommentByLineComment")
      text("Now comment each of these lines by pressing ${action("CommentByLineComment")} - ${action("GotoNextError")} - ${action("CommentByLineComment")}.")
    }
    task {
      triggers("GotoNextError")
      text("Cool! Now try pressing ${action("GotoNextError")} again - it should point you to the first warning in the file.")
    }
    task {
      triggers("ShowIntentionActions")
      text("Press ${action("EditorEscape")} and use ${action("ShowIntentionActions")} to fix the first warning.")
    }
    task {
      triggers("MethodDown")
      text("To quickly jump to the next method in the current file, press ${action("MethodDown")}.")
    }
    task {
      triggers("MethodUp")
      text("Jump to the previous method by using ${action("MethodUp")}.")
    }
    task {
      triggers("JumpToLastChange")
      text("Imagine you have a long file and you can't quite remember what you've changed in it and where. Simply press ${action("JumpToLastChange")} to jump to the most recent change in the file.")
    }
    task {
      triggers("GotoLine")
      text("Finally, when you need to jump to a specific line/column in your code, just press ${action("GotoLine")}.")
    }
  }
}