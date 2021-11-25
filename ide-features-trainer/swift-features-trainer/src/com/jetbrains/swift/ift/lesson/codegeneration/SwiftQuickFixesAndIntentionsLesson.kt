package com.jetbrains.swift.ift.lesson.codegeneration

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftQuickFixesAndIntentionsLesson : KLesson("swift.codegeneration.quickfixes",
                                                   SwiftLessonsBundle.message("swift.codegeneration.quickfix.name")) {

  private val sample: LessonSample = parseLessonSample("""
import Foundation

class QuickFixes: NSObject {
    func firstWarning() {
        var unused1 = 0
        var unused2 = 1
    }

    func secondWarning() {
        var unused3 = 1
        print(__LINE__)
    }

    func typoInspection() {
        var incorrctlySpelled = "incorrctlySpeled"
        print(incorrctlySpelled)
    }

    func errorHandlingInspections() {
        let contents = String(contentsOfFile: "someFile")
        print(contents)
    }

    func redundantAttributes() {
        class Super {
            @objc func foo() {
            }

            @objc func bar() {
            }
        }

        class Sub: Super {
            @objc override func foo() {
            }

            @objc override func bar() {
            }
        }
    }

    func addRemoveExplicitType() {
        let fileManager = FileManager.default
        print(fileManager)
        let string: String = ""
        print(string)
    }

}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.intro", code("fix-its")))
    caret(6, 17)
    task {
      triggers("ShowIntentionActions")
      text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.fixit", action("ShowIntentionActions"), code("_")))
    }
    task {
      triggers("CodeInspection.OnEditor")
      text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.sourcekit.inspection", action("CodeInspection.OnEditor")))
    }
    text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.sourcekit.fixit"))
    caret(15, 19)
    task {
      triggers("ShowIntentionActions", "EditorChooseLookupItem")
      text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.typo.in.var", action("EditorEscape"), action("ShowIntentionActions"), LessonUtil.rawEnter()))
    }
    caret(15, 41)
    task {
      triggers("ShowIntentionActions", "ShowIntentionActions")
      text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.typo.in.string", action("ShowIntentionActions"), code("incorrctlySpeled")))
    }
    task {
      triggers("GotoNextError")
      text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.go.next.error", action("GotoNextError")))
    }
    task {
      triggers("ShowIntentionActions")
      text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.error.handling", code("try/catch/throws"), action("EditorEscape"), action("ShowIntentionActions")))
    }
    task {
      triggers("GotoNextError")
      text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.go.next.warning", action("GotoNextError")))
    }
    task {
      triggers("com.intellij.codeInsight.daemon.impl.DaemonTooltipWithActionRenderer\$addActionsRow$1")
      text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.redundant", code("@objc")))
    }
    caret(43, 18)
    task {
      triggers("ShowIntentionActions")
      text(SwiftLessonsBundle.message("swift.codegeneration.quickfix.explicit.type", action("ShowIntentionActions"), code("fileManager")))
    }
  }

  override val suitableTips = listOf("QuickFix", "ContextActions")

  override val helpLinks: Map<String, String> get() = mapOf(
    Pair(SwiftLessonsBundle.message("swift.codegeneration.quickfix.help.link.1"),
         LessonUtil.getHelpLink("intention-actions.html")),
    Pair(SwiftLessonsBundle.message("swift.codegeneration.quickfix.help.link.2"),
         LessonUtil.getHelpLink("resolving-problems.html")),
  )
}