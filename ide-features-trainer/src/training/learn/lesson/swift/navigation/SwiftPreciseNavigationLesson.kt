package training.learn.lesson.swift.navigation

import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftPreciseNavigationLesson(module: Module) : KLesson("swift.navigation.precise", LessonsBundle.message("swift.navigation.precise.name"), module, "Swift") {

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
      text(LessonsBundle.message("swift.navigation.precise.intro"))
    }
    task {
      triggers("GotoNextError")
      text(LessonsBundle.message("swift.navigation.precise.next.error", action("GotoNextError")))
    }
    task {
      triggers("GotoNextError")
      text(LessonsBundle.message("swift.navigation.precise.next.error.again"))
    }
    task {
      triggers("GotoPreviousError")
      text(LessonsBundle.message("swift.navigation.precise.first.error", action("GotoPreviousError")))
    }
    task {
      triggers("CommentByLineComment", "GotoNextError", "CommentByLineComment")
      text(LessonsBundle.message("swift.navigation.precise.comment.combo", action("CommentByLineComment"), action("GotoNextError"), action("CommentByLineComment")))
    }
    task {
      triggers("GotoNextError")
      text(LessonsBundle.message("swift.navigation.precise.first.warning", action("GotoNextError")))
    }
    task {
      triggers("ShowIntentionActions")
      text(LessonsBundle.message("swift.navigation.precise.fix.warning", action("EditorEscape"), action("ShowIntentionActions")))
    }
    task {
      triggers("MethodDown")
      text(LessonsBundle.message("swift.navigation.precise.next.method", action("MethodDown")))
    }
    task {
      triggers("MethodUp")
      text(LessonsBundle.message("swift.navigation.precise.prev.method", action("MethodUp")))
    }
    task {
      triggers("JumpToLastChange")
      text(LessonsBundle.message("swift.navigation.precise.jump.last.changes", action("JumpToLastChange")))
    }
    task {
      triggers("GotoLine")
      text(LessonsBundle.message("swift.navigation.precise.jump.line", action("GotoLine")))
    }
  }
}