// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.refactorings

import com.intellij.openapi.project.Project
import com.intellij.refactoring.RefactoringBundle
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.impl.jList
import com.intellij.ui.components.JBList
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil
import org.jetbrains.plugins.ruby.ruby.refactoring.pushDown.RubyPushDownHandler
import org.jetbrains.ruby.ift.RubyLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonUtil.restoreIfModifiedOrMoved
import training.dsl.TaskTestContext
import training.dsl.defaultRestoreDelay
import training.dsl.parseLessonSample
import training.learn.LessonsBundle
import training.learn.course.KLesson

class RubyRefactorMenuLesson
  : KLesson("Refactoring menu", LessonsBundle.message("refactoring.menu.lesson.name"), "ruby") {

  private val sample = parseLessonSample("""
    class Animal
      def legs_number
        4
      end

      def <caret>meow
        'default meow'
      end
    end

    class Cat < Animal
    end
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      actionTask("Refactorings.QuickListPopupAction") {
        restoreIfModifiedOrMoved()
        RubyLessonsBundle.message("ruby.refactoring.menu.invoke.refactoring.list", action(it))
      }
      task(RefactoringBundle.message("push.members.down.title")) {
        text(RubyLessonsBundle.message("ruby.refactoring.menu.use.push.method.down", strong(it), code("meow()")))
        trigger("MemberPushDown") { checkMethodMoved(project) }
        restoreState(delayMillis = defaultRestoreDelay) {
          focusOwner !is JBList<*> && !checkInsidePushDownDialog()
        }
        test {
          ideFrame {
            jList("$it...").clickItem("$it...")
          }
          with(TaskTestContext.guiTestCase) {
            dialog(it) {
              button("Refactor").click()
            }
          }
        }
      }
    }

  private fun checkMethodMoved(project: Project): Boolean {
    val derived = SymbolUtil.findConstantByFQN(
      project,
      Types.MODULE_OR_CLASS_OR_CONSTANT,
      FQN.of("Cat"),
      null) ?: return false

    val barInDerived = SymbolUtil.findMethod(
      derived,
      "meow",
      Types.METHODS,
      null)

    return barInDerived?.parentSymbol?.name == "Cat"
  }

  private fun checkInsidePushDownDialog(): Boolean {
    return Thread.currentThread().stackTrace.find { element ->
      element.className == RubyPushDownHandler::class.java.name
    } != null
  }
}
