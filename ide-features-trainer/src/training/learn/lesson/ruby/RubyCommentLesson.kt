package training.learn.lesson.ruby

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