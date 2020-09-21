package training.learn.lesson.swift.codegeneration

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftCreateFromUsageLesson(module: Module) : KLesson("swift.codegeneration.createfromusage", "Create from Usage", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class CreateFromUsage: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addSubview(label)
        setup(view:label)
    }

    func createClassFromUsage() {
        var ide = IDE()

        var anotherIDE = IDE(helps:true)
    }
}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    task { caret(7, 30) }
    task {
      triggers("ShowIntentionActions", "EditorChooseLookupItem")
      text("In <ide/>, you can create various code constructs from their usages just by pressing ${action("ShowIntentionActions")} on the unresolved entity. Press ${action("ShowIntentionActions")}, select <strong>Create local variable 'label'</strong>, and then press ${LessonUtil.rawEnter()}")
    }
    task { type(" = UILabel()") }

    task { caret(9, 11) }
    task {
      triggers("ShowIntentionActions", "NextTemplateVariable")
      text("Repeat the same actions to create the ${code("setup")} function.")
    }
    task { text("Nice! Notice how parameters are generated together with their names.") }
    task { caret(17, 20) }
    task {
      triggers("ShowIntentionActions", "EditorChooseLookupItem")
      text("This time we can use the same approach to create a class declaration. Press ${action("ShowIntentionActions")} and select <strong>Create type 'IDE'</strong>. Note that you can automatically create it to be nested in the current class or in a new file.")
    }
    task { caret(22, 27) }
    task {
      triggers("ShowIntentionActions", "NextTemplateVariable")
      text("Great! Let's repeat the same actions to generate an initializer for our ${code("IDE")} class. Press ${action("ShowIntentionActions")}, select <strong>Create initializer</strong>, and then press ${LessonUtil.rawEnter()}!")
    }
    task { caret(22, 21) }
    task {
      triggers("ShowIntentionActions")
      text("Finally, let's use the same actions to add an empty initializer.")
    }
  }
}