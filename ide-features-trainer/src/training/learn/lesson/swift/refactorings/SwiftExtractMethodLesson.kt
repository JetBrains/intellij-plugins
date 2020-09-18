package training.learn.lesson.swift.refactorings

import com.intellij.icons.AllIcons
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftExtractMethodLesson(module: Module) : KLesson("swift.refactorings.extract.method", "Extract Method", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class ExtractMethod: UIViewController {

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

    task { select(11, 9, 18, 43) }

    task {
      triggers("ExtractMethod")
      text(
        "Extract Method is a refactoring that lets you encapsulate a code selection into a new method, with customizable parameters and return type. Let's extract some lines of code into a new ${
            code("setup")
        } function. Press ${action("ExtractMethod")}.")
    }
    task {
      triggers("\$Undo")
      text("Press ${action("\$Undo")} to undo the changes.")
    }
    task { select(11, 9, 18, 43) }

    task {
      text(
        "When extracting a method in <ide/>, you can change its signature. Click one of the parameters in the <strong>Parameters</strong> section and use ${
            icon(AllIcons.General.ArrowUp)
        }/${icon(AllIcons.General.ArrowDown)} icons to change the order of parameters. Change their names by clicking the parameter row. ")
    }
    task {
      triggers("ExtractMethod")
      text("Now press ${action("ExtractMethod")} and try changing the signature of the extracted method.")
    }


  }
}