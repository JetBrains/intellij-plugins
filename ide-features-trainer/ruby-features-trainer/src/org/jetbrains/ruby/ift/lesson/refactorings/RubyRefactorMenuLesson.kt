// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.refactorings

import com.intellij.openapi.project.Project
import com.intellij.refactoring.RefactoringBundle
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.impl.jList
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil
import org.jetbrains.ruby.ift.RubyLessonsBundle
import training.commands.kotlin.TaskTestContext
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class RubyRefactorMenuLesson(module: Module)
  : KLesson("Refactoring menu", LessonsBundle.message("refactoring.menu.lesson.name"), module, "ruby") {

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
        RubyLessonsBundle.message("ruby.refactoring.menu.invoke.refactoring.list", action(it))
      }
      task(RefactoringBundle.message("push.members.down.title")) {
        text(RubyLessonsBundle.message("ruby.refactoring.menu.use.push.method.down", strong(it), code("meow()")))
        trigger("MemberPushDown") { checkMethodMoved(project) }
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
}
