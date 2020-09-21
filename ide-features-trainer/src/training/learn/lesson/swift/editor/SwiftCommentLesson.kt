package training.learn.lesson.swift.editor

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftCommentLesson(module: Module) : KLesson("swift.editorbasics.commentline", "Comment", module, "Swift") {


  private val sample: LessonSample = parseLessonSample("""
import Foundation
import UIKit

class Comment: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let x = 0
        let y = 50

        Here we add a table view
        to our view controller
        let tableView = UITableView(frame: CGRect.zero)


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


    task { caret(12, 1) }
    task {
      triggers("CommentByLineComment")
      text("Comment out any line with ${action("CommentByLineComment")}.")
    }
    task { caret(12, 1) }
    task {
      triggers("CommentByLineComment")
      text("Uncomment the commented line with the same shortcut, ${action("CommentByLineComment")}.")
    }
    task { caret(12, 1) }
    task {
      triggers("EditorDownWithSelection", "CommentByLineComment")
      text("Select several lines with ${action("EditorDownWithSelection")} and then comment them with ${action("CommentByLineComment")}.")
    }
    task { select(1, 1, 1, 1) }

    task { caret(14, 37) }
    task {
      triggers("EditorSelectWord", "EditorSelectWord", "CommentByBlockComment")
      text("Press ${action("EditorSelectWord")} twice to select ${code("frame: CGRect.zero")}, and then comment it with a block comment by using ${action("CommentByBlockComment")}.")
    }
  }
}