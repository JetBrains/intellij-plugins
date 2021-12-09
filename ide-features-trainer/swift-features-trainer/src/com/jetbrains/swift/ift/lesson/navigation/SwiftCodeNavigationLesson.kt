package com.jetbrains.swift.ift.lesson.navigation

import com.jetbrains.swift.ift.SwiftLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class SwiftCodeNavigationLesson : KLesson("swift.navigation.code", SwiftLessonsBundle.message("swift.navigation.code.name")) {

  private val sample: LessonSample = parseLessonSample("""
import Foundation

class Feature {
    var name = ""
}

protocol IDEProtocol {
    func navigation() -> Feature
    func assistance() -> Feature
    func generation() -> Feature
}

class JetBrainsIDE: IDEProtocol {
    func navigation() -> Feature {
        return Feature()
    }

    func assistance() -> Feature {
        return Feature()
    }

    func generation() -> Feature {
        return Feature()
    }
}

class AppCode: JetBrainsIDE {}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    text(SwiftLessonsBundle.message("swift.navigation.code.intro"))
    task {
      triggers("GotoClass", "DetailViewController.swift")
      text(SwiftLessonsBundle.message("swift.navigation.code.class", code("DetailViewController"), action("GotoClass"), code("dvc"),
                                      LessonUtil.rawEnter()))
    }
    text(SwiftLessonsBundle.message("swift.navigation.code.fuzzy"))
    task {
      triggers("GotoFile", "AppDelegate.swift")
      text(SwiftLessonsBundle.message("swift.navigation.code.file", code("AppDelegate.swift"), action("GotoFile"), code("ad"),
                                      LessonUtil.rawEnter()))
    }
    task {
      triggers("GotoSymbol", "MasterViewController.swift")
      text(SwiftLessonsBundle.message("swift.navigation.code.symbol", code("detailViewController"), code("MasterViewController"),
                                      action("GotoSymbol"), code("dvc"), LessonUtil.rawEnter()))
    }
    text(SwiftLessonsBundle.message("swift.navigation.code.non.project.files"))
    caret(5, 20)
    task {
      triggers("GotoDeclaration", "DetailViewController.swift")
      text(SwiftLessonsBundle.message("swift.navigation.code.declaration", code("DetailViewController?"), action("GotoDeclaration")))
    }
    caret(3, 33)
    task {
      triggers("GotoImplementation")
      text(SwiftLessonsBundle.message("swift.navigation.code.implementation", action("GotoDeclaration"), action("GotoImplementation"), code("UIViewController")))
    }
    task {
      triggers("GotoFile", "Navigation.swift")
      text(SwiftLessonsBundle.message("swift.navigation.code.go.back", code("Navigation.swift"), action("GotoFile")))
    }
    caret(27, 10)
    task {
      triggers("GotoSuperMethod")
      text(SwiftLessonsBundle.message("swift.navigation.code.super", action("GotoSuperMethod"), code("JetBrainsIDE")))
    }
    task {
      triggers("GotoSuperMethod")
      text(SwiftLessonsBundle.message("swift.navigation.code.super.again", action("GotoSuperMethod"), code("IDEProtocol")))
    }
    task {
      triggers("RecentFiles")
      text(SwiftLessonsBundle.message("swift.navigation.code.recent", action("RecentFiles")))
    }
    task {
      triggers("Switcher")
      text(SwiftLessonsBundle.message("swift.navigation.code.switcher", action("Switcher")))
    }
  }
}