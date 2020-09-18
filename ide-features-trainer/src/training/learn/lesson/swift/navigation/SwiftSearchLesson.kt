package training.learn.lesson.swift.navigation

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftSearchLesson(module: Module) : KLesson("swift.navigation.search", "Search", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import Foundation

protocol Test {
    func test(containsTest test: Int, anotherContainingTest test1: String)
}

typealias TestAlias = AnotherTest

class AnotherTest: Test {
    func test(containsTest test: Int, anotherContainingTest testAnother: String) {
        print(test)

    }

    func tests() -> Test {
        let test = AnotherTest()
        test.test(containsTest: 1, anotherContainingTest: "test")
        return test
    }
}
""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    task {
      text(
        "Searching in AppCode is different. We have special features for searching text and others dedicated to searching code. This is because code is not mere text - it has a specific structure formed by the code constructs.")
    }
    task { select(16, 13, 16, 17) }

    task {
      triggers("Find")
      text("This sample file has lots of code constructs containing the word ${code("test")}. The caret is on the variable named ${
          code("test")
      }. Press ${action("Find")} to find all the text occurrences of the word ${code("test")}.")
    }
    task { select(16, 14, 16, 14) }

    task {
      triggers("FindUsages")
      text("This is not very helpful, is it? In real code, we probably want to search for the ${
          code("test")
      } variable, without including class names, method names, or any other code constructs. Try pressing ${
          action("EditorEscape")
      } and then ${action("FindUsages")}.")
    }
    task { text("Now, AppCode shows us only actual usages of the ${code("test")} variable.") }
    task { caret(15, 23) }
    task {
      triggers("FindUsages")
      text("The same applies to any other code construct. Press ${action("EditorEscape")} → ${
          action("FindUsages")
      } to find all the usages of the ${code("Test")} protocol in the project.")
    }
    task { caret(17, 17) }
    task {
      triggers("FindUsages")
      text("Now let's find all usages of the method ${
          code("test(containsTest test: Int, anotherContainingTest test1: String)")
      }. Press ${action("EditorEscape")} → ${action("FindUsages")} again.")
    }
    task {
      text(
        "The simple rule is: think which symbol you need to find, instead of which piece of text you need to find. <strong>Find Usages</strong> works for any code construct in the project context, so you will find all the usages in the whole codebase.")
    }
    task {
      text(
        "Of course, sometimes you don't know what exactly you're looking for, and you want to find as many matches as possible by searching for some text. In this case, use ${
            action("Find")
        } to search in the current file, or use ${action("FindInPath")} to search in the whole project.")
    }
    task {
      triggers("SearchEverywhere")
      text(
        "What if you want to search for some text in symbols, file names, or even IDE settings and menu items? Press <shortcut>Double⇧</shortcut> to open the <strong>Search Everywhere</strong> dialog.")
    }
    task {
      triggers("GotoAction")
      text("Finally, try using <strong>Find Action</strong> (${
          action("GotoAction")
      }) to quickly find an IDE action and execute it. For example, type 'plugins' to open <strong>Preferences | Plugins</strong>.")
    }


  }
}