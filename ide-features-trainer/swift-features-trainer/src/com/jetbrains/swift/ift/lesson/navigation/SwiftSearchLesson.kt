package com.jetbrains.swift.ift.lesson.navigation

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftSearchLesson : KLesson("swift.navigation.search", SwiftLessonsBundle.message("swift.navigation.search.name"),
                                  "Swift") {

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

    text(SwiftLessonsBundle.message("swift.navigation.search.intro"))
    select(16, 13, 16, 17)

    task {
      triggers("Find")
      text(SwiftLessonsBundle.message("swift.navigation.search.var", code("test"), code("test"), action("Find"), code("test")))
    }
    select(16, 14, 16, 14)

    task {
      triggers("FindUsages")
      text(SwiftLessonsBundle.message("swift.navigation.search.find.usages.var", code("test"), action("EditorEscape"), action("FindUsages")))
    }
    text(SwiftLessonsBundle.message("swift.navigation.search.find.usages.var.result", code("test")))
    caret(15, 23)
    task {
      triggers("FindUsages")
      text(SwiftLessonsBundle.message("swift.navigation.search.find.usages.proto", action("EditorEscape"), action("FindUsages"), code("Test")))
    }
    caret(17, 17)
    task {
      triggers("FindUsages")
      text(SwiftLessonsBundle.message("swift.navigation.search.find.usages.method",
                                 code("test(containsTest test: Int, anotherContainingTest test1: String)"), action("EditorEscape"),
                                 action("FindUsages")))
    }
    text(SwiftLessonsBundle.message("swift.navigation.search.rule"))
    text(SwiftLessonsBundle.message("swift.navigation.search.text.search", action("Find"), action("FindInPath")))
    task {
      triggers("SearchEverywhere")
      text(SwiftLessonsBundle.message("swift.navigation.search.everything"))
    }
    task {
      triggers("GotoAction")
      text(SwiftLessonsBundle.message("swift.navigation.search.action", action("GotoAction")))
    }
  }
}