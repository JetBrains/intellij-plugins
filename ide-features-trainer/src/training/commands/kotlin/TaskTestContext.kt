package training.commands.kotlin

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.TransactionGuard
import com.intellij.testGuiFramework.fixtures.IdeFrameFixture
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.framework.Timeouts
import com.intellij.testGuiFramework.impl.GuiTestCase
import com.intellij.testGuiFramework.impl.waitUntilFound
import java.awt.Component

class TaskTestContext(val task: TaskContext) {

  fun type(text: String) {
    GuiTestUtil.typeText(text)
  }

  fun actions(vararg actionIds: String) {
    for (actionId in actionIds) {
      val action = ActionManager.getInstance().getAction(actionId) ?: error("Action $actionId is non found")
      DataManager.getInstance().dataContextFromFocusAsync.onSuccess { dataContext ->
        TransactionGuard.submitTransaction(task.project, Runnable {
          val event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, dataContext)
          ActionUtil.performActionDumbAwareWithCallbacks(action, event, dataContext)
        })
      }
    }
  }

  fun ideFrame(action: IdeFrameFixture.() -> Unit) {
    with(guiTestCase) {
      ideFrame { // Note: It is not recursive call here. It is GuiTestCase#ideFrame
        action()
      }
    }
  }

  fun <ComponentType : Component> waitComponent(componentClass: Class<ComponentType>, partOfName: String) {
    waitUntilFound(null, componentClass, Timeouts.seconds02) {
      it.javaClass.name.contains(partOfName) && it.isShowing
    }
  }

  companion object {
    val guiTestCase: GuiTestCase by lazy { GuiTestCase() }
  }
}
