/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson.ruby.basic

import com.intellij.psi.tree.IElementType
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes
import training.learn.interfaces.Module
import training.learn.lesson.general.SingleLineCommentLesson
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class RubyCommentLesson(module: Module) : SingleLineCommentLesson(module, "ruby") {
  override val commentElementType: IElementType
    get() = RubyTokenTypes.TLINE_COMMENT
  override val sample: LessonSample
    get() = parseLessonSample("""
def foo
  p "Some method"
end

def hello
  p "Hello world"
end
""")
}