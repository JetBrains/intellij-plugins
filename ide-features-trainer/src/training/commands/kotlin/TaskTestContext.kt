// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands.kotlin

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.TransactionGuard
import com.intellij.testGuiFramework.cellReader.ExtendedJListCellReader
import com.intellij.testGuiFramework.fixtures.ComponentFixture
import com.intellij.testGuiFramework.fixtures.IdeFrameFixture
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.framework.Timeouts
import com.intellij.testGuiFramework.impl.GuiTestCase
import com.intellij.testGuiFramework.impl.findComponentWithTimeout
import com.intellij.testGuiFramework.impl.waitUntilFound
import com.intellij.testGuiFramework.util.step
import org.fest.swing.core.Robot
import org.fest.swing.fixture.ContainerFixture
import org.fest.swing.fixture.JListFixture
import org.fest.swing.timing.Timeout
import java.awt.Component
import java.awt.Container
import javax.swing.JList

class TaskTestContext(val task: TaskContext) {

  data class TestScriptProperties (
    val duration: Int = 6 //seconds
  )

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
      ideFrame {
        // Note: It is not recursive call here. It is GuiTestCase#ideFrame
        action()
      }
    }
  }

  fun <ComponentType : Component> waitComponent(componentClass: Class<ComponentType>, partOfName: String) {
    waitUntilFound(null, componentClass, Timeouts.seconds02) {
      it.javaClass.name.contains(partOfName) && it.isShowing
    }
  }

  // Modified copy-paste
  fun <C : Container> ContainerFixture<C>.jListContains(partOfItem: String? = null, timeout: Timeout = Timeouts.seconds02): JListFixture {
    return step("search '$partOfItem' in list") {
      val extCellReader = ExtendedJListCellReader()
      val myJList: JList<*> = findComponentWithTimeout(timeout) { jList: JList<*> ->
        if (partOfItem == null) true //if were searching for any jList()
        else {
          val elements = (0 until jList.model.size).map { it: Int -> extCellReader.valueAt(jList, it) }
          elements.any { it.toString().contains(partOfItem) } && jList.isShowing
        }
      }
      val jListFixture = JListFixture(robot(), myJList)
      jListFixture.replaceCellReader(extCellReader)
      return@step jListFixture
    }
  }


  class SimpleComponentFixture(robot: Robot, target: Component): ComponentFixture<SimpleComponentFixture, Component>(SimpleComponentFixture::class.java, robot, target)

  fun IdeFrameFixture.jComponent(target: Component): SimpleComponentFixture {
    return SimpleComponentFixture(robot(), target)
  }

  companion object {
    val guiTestCase: GuiTestCase by lazy { GuiTestCase() }
  }
}
