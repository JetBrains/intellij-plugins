package training.learn.lesson.swift.editor

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftMultipleSelectionsLesson(module: Module) : KLesson("swift.editorbasics.multipleselections", "Multiple Selections", module,
                                                              "Swift") {

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


    task { caret(14, 18) }
    task {
      triggers("SelectNextOccurrence")
      text("Press ${action("SelectNextOccurrence")} to select the symbol at the caret.")
    }
    task {
      triggers("SelectNextOccurrence")
      text("Press ${action("SelectNextOccurrence")} again to select the next occurrence of this symbol.")
    }
    task {
      triggers("UnselectPreviousOccurrence")
      text("Press ${action("UnselectPreviousOccurrence")} to deselect the last occurrence.")
    }
    task {
      triggers("SelectAllOccurrences")
      text("Press ${action("SelectAllOccurrences")} to select all occurrences in the file.")
    }
    task {
      triggers("EditorEscape")
      text("Type ${code("td")} to replace all occurrences of ${code("th")} with ${code("td")}, and then press ${action("EditorEscape")}.")
    }
    task { caret(29, 1) }
    task {
      triggers("EditorToggleColumnMode", "EditorDownWithSelection", "EditorDownWithSelection", "EditorDownWithSelection",
               "EditorDownWithSelection", "EditorDownWithSelection")
      text(
        "Toggle Column Selection using ${action("EditorToggleColumnMode")} and select all the lines from ${code("ONE")} to ${code("SIX")}.")
    }
    task {
      triggers("EditorNextWordWithSelection")
      text("Now type ${code("case")} and select all case names with ${action("EditorNextWordWithSelection")}")
    }
    task {
      triggers("EditorToggleCase", "EditorToggleColumnMode")
      text("Press ${action("EditorToggleCase")} to toggle the case, and then exit the Column Selection mode by pressing ${
        action("EditorToggleColumnMode")
      } again.")
    }
    task {
      triggers("ReformatCode")
      text("Press ${action("EditorEscape")}, reformat the code with ${action("ReformatCode")} and that's it!")
    }


  }
}