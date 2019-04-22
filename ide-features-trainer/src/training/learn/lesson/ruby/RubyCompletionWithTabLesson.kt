package training.learn.lesson.ruby

import training.lang.JavaLangSupport
import training.lang.RubyLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.general.CompletionWithTabLesson
import training.learn.lesson.kimpl.parseLessonSample

class RubyCompletionWithTabLesson(module: Module) :
    CompletionWithTabLesson(module, JavaLangSupport.lang, "goodbye") {

  override val sample = parseLessonSample("""class DemoClass
  def hello
    print 'Hello'
  end

  def goodbye
    print 'Goodbye'
  end
end

# @param demo [DemoClass]
def say_something(demo)
  demo.<caret>hello
end
""".trimIndent())

  override val existedFile = RubyLangSupport.sandboxFile
}