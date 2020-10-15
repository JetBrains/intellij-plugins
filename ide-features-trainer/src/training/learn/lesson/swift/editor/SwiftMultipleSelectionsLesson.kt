package training.learn.lesson.swift.editor

import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftMultipleSelectionsLesson(module: Module) : KLesson("swift.editorbasics.multipleselections", LessonsBundle.message("swift.editor.selections.name"), module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""import Foundation

var html = 
""" + "\"\"\"" +
                                                       """
<!doctype html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Multiple selections</title>
    </head>
    <body>
        <table>
            <tr>
                <th>Firstname</th>
                <th>Lastname</th>
                <th>Points</th>
            </tr>
            <tr>
                <td>Eve</td>
                <td>Jackson</td>
                <td>94</td>
            </tr>
        </table>
    </body>
</html>""" +
                                                       "\n\"\"\"\n\n" +
                                                       """
enum ColumnSelection {
ONE
TWO
THREE
FOUR
FIVE
SIX
}

""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    caret(14, 18)
    task {
      triggers("SelectNextOccurrence")
      text(LessonsBundle.message("swift.editor.selections.next", action("SelectNextOccurrence")))
    }
    task {
      triggers("SelectNextOccurrence")
      text(LessonsBundle.message("swift.editor.selections.next.again", action("SelectNextOccurrence")))
    }
    task {
      triggers("UnselectPreviousOccurrence")
      text(LessonsBundle.message("swift.editor.selections.unselect", action("UnselectPreviousOccurrence")))
    }
    task {
      triggers("SelectAllOccurrences")
      text(LessonsBundle.message("swift.editor.selections.select.all", action("SelectAllOccurrences")))
    }
    task {
      triggers("EditorEscape")
      text(LessonsBundle.message("swift.editor.selections.replace", code("td"), code("th"), code("td"), action("EditorEscape")))
    }
    caret(29, 1)
    task {
      triggers("EditorToggleColumnMode", "EditorDownWithSelection", "EditorDownWithSelection", "EditorDownWithSelection", "EditorDownWithSelection", "EditorDownWithSelection")
      text(LessonsBundle.message("swift.editor.selections.column", action("EditorToggleColumnMode"), code("ONE"), code("SIX")))
    }
    task {
      triggers("EditorNextWordWithSelection")
      text(LessonsBundle.message("swift.editor.selections.select.sample", code("case"), action("EditorNextWordWithSelection")))
    }
    task {
      triggers("EditorToggleCase", "EditorToggleColumnMode")
      text(LessonsBundle.message("swift.editor.selections.select.sample.toggle", action("EditorToggleCase"), action("EditorToggleColumnMode")))
    }
    task {
      triggers("ReformatCode")
      text(LessonsBundle.message("swift.editor.selections.reformat", action("EditorEscape"), action("ReformatCode")))
    }
  }
}