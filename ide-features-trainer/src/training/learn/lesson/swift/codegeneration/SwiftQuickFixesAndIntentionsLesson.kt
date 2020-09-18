package training.learn.lesson.swift.codegeneration

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftQuickFixesAndIntentionsLesson(module: Module): KLesson("swift.codegeneration.quickfixes", "Quick-fixes and Intentions", module, "Swift") {

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

    task { text("<ide/> shows all the same ${code("fix-its")} for your Swift code as Xcode does.")}
    task { caret(6,17)}
    task {
triggers("ShowIntentionActions")
text("Press ${action("ShowIntentionActions")} and select <strong>Apply Fix-it</strong> to replace the unused variable with ${code("_")}")
}
    task {
triggers("CodeInspection.OnEditor")
text("AppCode also integrates SourceKit as a separate inspection. This means you can run it on the whole file and fix all the problems at once if several fix-its are available. Press ${action("CodeInspection.OnEditor")}.")
}
    task { text("As you can see, we have several problems in this piece of code. Let's fix some of them. Select <strong>Swift → SourceKit inspections</strong> and click the <strong>Apply Fix-it</strong> button on the right. This should correct all the problems in this group.")}
    task { caret(15,19)}
    task {
triggers("ShowIntentionActions", "EditorChooseLookupItem")
text("Now press ${action("EditorEscape")} to return to the editor window, place the caret on the incorrectly spelled variable name, and then press ${action("ShowIntentionActions")}. Select <strong>Typo: Rename to...</strong>, choose the correct option, and then press ${LessonUtil.rawEnter()}")
}
    task { caret(15,41)}
    task {
triggers("ShowIntentionActions", "ShowIntentionActions")
text("Note how only the needed part was corrected and all variable usages were automatically renamed. Now, repeat the same actions twice to fix the ${code("incorrctlySpeled")} string.")
}
    task {
triggers("GotoNextError")
text("Press ${action("GotoNextError")} to go to the next error.")
}
    task {
triggers("ShowIntentionActions")
text("Error handling intentions can help you add ${code("try/catch/throws")} where needed. Press ${action("EditorEscape")}→${action("ShowIntentionActions")} and select one of the quick-fixes.")
}
    task {
triggers("GotoNextError")
text("Press ${action("GotoNextError")} to go to the next warning.")
}
    task {
triggers("com.intellij.codeInsight.daemon.impl.DaemonTooltipWithActionRenderer\$addActionsRow$1")
text("You can also remove redundant ${code("@objc")} attributes. Press <shortcut>⇧⌥⏎</shortcut>.")
}
    task {
triggers("ShowIntentionActions")
text("If you need to add or remove an explicit type to/from some variable, simply press ${action("ShowIntentionActions")} and select <strong>Add/Remove explicit type</strong>. Try it now with the ${code("fileManager")} variable.")
}
    

}
}