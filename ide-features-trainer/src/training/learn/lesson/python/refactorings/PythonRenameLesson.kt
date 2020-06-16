// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.refactorings

import com.intellij.ide.DataManager
import com.intellij.ide.actions.exclusion.ExclusionHandler
import com.intellij.openapi.application.runReadAction
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.framework.Timeouts
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.impl.jTree
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.tree.TreeVisitor
import com.intellij.util.ui.tree.TreeUtil
import org.jetbrains.annotations.Nullable
import training.commands.kotlin.TaskTestContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample
import java.util.regex.Pattern
import javax.swing.JButton
import javax.swing.JTree
import javax.swing.tree.TreePath

class PythonRenameLesson(module: Module) : KLesson("Rename", module, "Python") {
  private val template = """
      class Championship:
          def __init__(self):
              self.<name> = 0
      
          def matches_count(self):
              return self.<name> * (self.<name> - 1) / 2
          
          def add_new_team(self):
              self.<name> += 1
      
      def team_matches(champ):
          champ.<name>() - 1

      class Company:
          def __init__(self, t):
              self.teams = t
      
      def company_members(company):
          map(lambda team : team.name, company.teams)

      def teams():
          return 16
      
      c = Championship()
      
      c.<caret><name> = teams()
      
      print(c.<name>)
  """.trimIndent() + '\n'

  /** For test only */
  private val substringPredicate: (String, String) -> Boolean = { found: String, wanted: String -> found.contains(wanted) }

  private val sample = parseLessonSample(template.replace("<name>", "teams"))

  private val replacePreviewPattern = Pattern.compile(".*Variable to be renamed to (\\w+).*")

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    var replace: String? = null
    task("RenameElement") {
      text("Press ${action(it)} to rename field <code>teams</code> (e.g., to <code>teams_number</code>).")
      triggerByFoundPathAndHighlight { tree: JTree, path: TreePath ->
        if (path.pathCount == 2 && path.getPathComponent(1).toString().contains("Dynamic")) {
          replace = replacePreviewPattern.matcher(tree.model.root.toString()).takeIf { m -> m.find() }?.group(1)
          true
        }
        else false
      }
      test {
        actions(it)
        with(TaskTestContext.guiTestCase) {
          dialog {
            typeText("teams_number")
            button("Refactor").click()
          }
        }
      }
    }

    task {
      // Increase deterministic: collapse nodes
      before {
        (previous.ui as? JTree)?.let { tree ->
          TreeUtil.collapseAll(tree, 1)
        }
      }
      text("In simple case PyCharm will just rename without confirmation. But in this sample PyCharm see two calls of  " +
           "${code("teams")} method for objects with unknown type. Expand <strong>Dynamic references</strong> item.")

      triggerByFoundPathAndHighlight { _: JTree, path: TreePath ->
        path.pathCount == 6 && path.getPathComponent(5).toString().contains("company_members")
      }
      test {
        ideFrame {
          val jTree = runReadAction {
            jTree("Dynamic references", timeout = Timeouts.seconds03, predicate = substringPredicate)
          }
          // WARNING: several exception will be here because of UsageNode#toString inside info output during this operation
          jTree.doubleClickPath()
        }
      }
    }

    task {
      text("It seems ${code("company_members")} should be excluded from rename. " +
           "Select it and press ${action("EditorDelete")}.")

      stateCheck {
        val tree = previous.ui as? JTree ?: return@stateCheck false
        val last = pathToExclude(tree) ?: return@stateCheck false
        val dataContext = DataManager.getInstance().getDataContext(tree)
        val exclusionProcessor: ExclusionHandler<*> = ExclusionHandler.EXCLUSION_HANDLER.getData(dataContext) ?: return@stateCheck false
        val leafToBeExcluded = last.lastPathComponent
        @Suppress("UNCHECKED_CAST")
        fun <T : Any?> castHack(processor: ExclusionHandler<T>): Boolean {
          return processor.isNodeExclusionAvailable(leafToBeExcluded as T) && processor.isNodeExcluded(leafToBeExcluded as T)
        }
        castHack(exclusionProcessor)
      }
      test {
        ideFrame {
          type("co_me")
          GuiTestUtil.shortcut(Key.DELETE)
        }
      }
    }

    task {
      triggerByUiComponentAndHighlight(highlightInside = false) { button: JButton ->
        button.text == "Do Refactor"
      }
    }

    task {
      val result = replace?.let { template.replace("<name>", it).replace("<caret>", "") }
      text("Now just finish rename with <strong>Do Refactor</strong> button.")
      stateCheck { editor.document.text == result }
      test {
        ideFrame {
          button("Do Refactor").click()
        }
      }
    }
  }

  private fun pathToExclude(tree: JTree): @Nullable TreePath? {
    return TreeUtil.promiseVisit(tree, TreeVisitor { path ->
      if (path.pathCount == 7 && path.getPathComponent(6).toString().contains("lambda"))
        TreeVisitor.Action.INTERRUPT
      else
        TreeVisitor.Action.CONTINUE
    }).blockingGet(200)
  }
}
