package training.learn.lesson.swift.rundebugtest

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftDebugLesson(module: Module): KLesson("swift.rdt.debug", "Debug", module, "Swift") {

    private val sample: LessonSample = parseLessonSample("""
import UIKit

class DebugExample: UIViewController {

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

    task { text("Now let's learn some debug basics.")}
    task {
triggers("GotoFile", "MasterViewController.swift")
text("Navigate to ${code("MasterViewController.swift")} by pressing ${action("GotoFile")}.")
}
    task { caret(11,9)}
    task {
triggers("ToggleLineBreakpoint", "Debug")
text("Toggle a breakpoint at line 11 with ${action("ToggleLineBreakpoint")} and then press ${action("Debug")}.")
}
    task {
triggers("StepInto", "StepOver")
text("Try to step into, by using ${action("StepInto")}, and then step over with ${action("StepOver")}.")
}
    task {
triggers("RunToCursor")
text("Now, set the caret to line 21 and execute <strong>Run to cursor</strong> - ${action("RunToCursor")}.")
}
    task {
triggers("StepOver")
text("Step over to the next line.")
}
    task {
triggers("EditorSelectWord", "EditorSelectWord", "EvaluateExpression")
text("Select ${code("controllers[controllers.count-1]")} using ${action("EditorSelectWord")} and use <strong>Evaluate Expression</strong> (${action("EvaluateExpression")} → ${LessonUtil.rawEnter()}).")
}
    task {
triggers("EditorChooseLookupItem")
text("Enter the dot symbol, select some property, and then press ${LessonUtil.rawEnter()} to evaluate it without switching to LLDB console.")
}
    task {
triggers("Stop")
text("Press ${action("Stop")} to stop debugging the application.")
}
    

}
}