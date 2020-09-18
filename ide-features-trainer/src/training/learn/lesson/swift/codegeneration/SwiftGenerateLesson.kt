package training.learn.lesson.swift.codegeneration

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftGenerateLesson(module: Module): KLesson("swift.codegeneration.generate", "Generate", module, "Swift") {

    private val sample: LessonSample = parseLessonSample("""
import Foundation

class Action {
    var name: String?
}

class IDE {
    let version = 2018
    let build = "123"
    var codeGenerationActions:Array<Action>
    var name = "AppCode"
}""".trimIndent())
override val lessonContent: LessonContext.() -> Unit = {
prepareSample(sample)

    task { caret(12,1)}
    task { text("You can generate ${code("equals")}, ${code("hashValue")}, ${code("description")}, and ${code("debugDescription")} properties, as well as initializers by using the <strong>Generate</strong> action.")}
    task {
triggers("Generate", "Swift.Generate.Init")
text("Press ${action("Generate")} and select <strong>Initializer</strong> action in the in the <strong>Generate</strong> popup. Select properties to include into the initializer signature and press ${LessonUtil.rawEnter()}")
}
    task {
triggers("Swift.Generate.EqualsHashValue")
text("Generate ${code("equals")} and ${code("hash")} using ${action("Generate")} → <strong>equals and hash</strong>")
}
    task {
triggers("Swift.Generate.Description")
text("Finally, generate ${code("description")} using ${action("Generate")} → <strong>description</strong> action.")
}
    

}
}