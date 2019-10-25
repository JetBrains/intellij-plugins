package training.learn.lesson.go.release

import com.goide.psi.GoFile
import com.intellij.psi.PsiDocumentManager
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class GoPostfixTemplates(module: Module) : KLesson("Postfix templates", module, "go") {
    private val sample = parseLessonSample("""package main

import (
	"os"
)

func read(file *os.File, data []byte) int {
	file.Write(data).
	1.
}

func main() {
	data := make([]byte, 100)
	message := "Error"
	file, err := os.Open("example.txt")
    err.
	read(file, data)
}""".trimIndent())

    override val lessonContent: LessonContext.() -> Unit
        get() = {
            prepareSample(sample)
            fun checkState (state : String, functionName : String): Boolean  {
                val manager = PsiDocumentManager.getInstance(project)
                val file = manager.getPsiFile(editor.document) as? GoFile ?: return false
                val block = file.functions.find { it.name == functionName }?.block ?: return false
                return block.text == state
            }
            task {
                caret(16, 9)
                text("With postfix templates, you can transform an already-typed expression to a different one based" +
                        " on a postfix after the dot, the expression type, and its context. You can use a set of predefined postfix completion templates or create new templates. " +
                        "For more information about custom postfix templates, see <a href = \"https://www.jetbrains.com/help/go/auto-completing-code.html#custom_postfix_templates\">this documentation article</a>.\n" +
                        "Type <code>nn</code> and select the <code>nn</code> postfix template from the completion list.")
                trigger("EditorChooseLookupItem") { checkState("{\n" +
                        "\tdata := make([]byte, 100)\n" +
                        "\tmessage := \"Error\"\n" +
                        "\tfile, err := os.Open(\"example.txt\")\n" +
                        "\tif err != nil {\n" +
                        "\t\t\n" +
                        "\t}\n" +
                        "\tread(file, data)\n" +
                        "}", "main")
                }
            }
            task {
                caret(17, 9)
                text("Type <code>message.panic</code> and press <action>EditorEnter</action>. Select <code>panic</code> from the list of suggestions.")
                trigger("EditorChooseLookupItem") { checkState("{\n" +
                        "\tdata := make([]byte, 100)\n" +
                        "\tmessage := \"Error\"\n" +
                        "\tfile, err := os.Open(\"example.txt\")\n" +
                        "\tif err != nil {\n" +
                        "\t\tpanic(message)\n" +
                        "\t}\n" +
                        "\tread(file, data)\n" +
                        "}", "main")
                }
            }
            task {
                caret(8, 22)
                text("The <code>rr</code> postfix completion template generates a piece of code that checks if the error variable"+
                        " is not <code>nil</code>. " +
                        "You can use the <code>rr</code> postfix completion template for error handling.\n" +
                        "Now type <code>rr</code> and press <action>EditorEnter</action> to select the postfix template.")
                trigger("EditorChooseLookupItem") { checkState ("{\n" +
                        "\tif _, err := file.Write(data); err != nil {\n" +
                        "\t\treturn 0\n" +
                        "\t}\n" +
                        "\t1.\n" +
                        "}", "read")
                }
            }
            task {
                caret(11,7)
                text("Type <code>return</code> and apply the <code>return</code> postfix template.")
                trigger("EditorChooseLookupItem") { checkState ("{\n" +
                        "\tif _, err := file.Write(data); err != nil {\n" +
                        "\t\treturn 0\n" +
                        "\t}\n" +
                        "\treturn 1\n" +
                        "}", "read")
                }
            }
        }
}
