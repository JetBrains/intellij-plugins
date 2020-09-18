package training.learn.lesson.swift.navigation

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*

class SwiftCodeNavigationLesson(module: Module) : KLesson("swift.navigation.code", "Code Navigation", module, "Swift") {

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

    task {
      text(
        "When working on your project, you often need to open and edit a specific class, file, or symbol. The fastest way to do this is to use the main navigation triple: <strong>Go to Class/File/Symbol</strong>.")
    }
    task {
      triggers("GotoClass", "DetailViewController.swift")
      text("Let's navigate to the ${code("DetailViewController")} class. Press ${action("GotoClass")}, type ${
        code("dvc")
      }, and then press ${LessonUtil.rawEnter()}.")
    }
    task {
      text(
        "Each of the <strong>Go to...</strong> dialogs allows using fuzzy matching, so you can type only a part of the name of a class, file, or symbol in order to find it.")
    }
    task {
      triggers("GotoFile", "AppDelegate.swift")
      text("Nice! Now let's open the ${code("AppDelegate.swift")} file: press ${action("GotoFile")}, type ${
        code("ad")
      } and press ${LessonUtil.rawEnter()}.")
    }
    task {
      triggers("GotoSymbol", "MasterViewController.swift")
      text("Let's jump directly to the ${code("detailViewController")} declaration in the ${
        code("MasterViewController")
      } class. Press ${action("GotoSymbol")}, type ${code("dvc")}, and then press ${LessonUtil.rawEnter()}.")
    }
    task {
      text(
        "If you also need to include standard libraries and other non-project files, symbols, or classes, just press the shortcut twice.")
    }
    task { caret(5, 20) }
    task {
      triggers("GotoDeclaration", "DetailViewController.swift")
      text("Another important action from the <strong>Go to...</strong> family is <strong>Go to Declaration</strong>. Place the caret at ${
        code("DetailViewController?")
      } and press ${action("GotoDeclaration")} to jump to its declaration.")
    }
    task { caret(3, 33) }
    task {
      triggers("GotoImplementation")
      text("If a method or class has several implementations, ${action("GotoDeclaration")} will get you to the first one. Try using ${
        action("GotoImplementation")
      } to see all the definitions of ${code("UIViewController")}.")
    }
    task {
      triggers("GotoFile", "Navigation.swift")
      text("Let's navigate back to ${code("Navigation.swift")} via ${action("GotoFile")}.")
    }
    task { caret(27, 10) }
    task {
      triggers("GotoSuperMethod")
      text("<strong>Go to Super Definition</strong> navigates you to the parent class declaration. Press ${
        action("GotoSuperMethod")
      } to jump to the ${code("JetBrainsIDE")} class.")
    }
    task {
      triggers("GotoSuperMethod")
      text("Now press ${action("GotoSuperMethod")} again to jump to the ${code("IDEProtocol")} protocol declaration.")
    }
    task {
      triggers("RecentFiles")
      text(
        "Finally, there are two very useful and frequently used navigation actions that can help you quickly switch between recent files and toolwindows. First up is the <strong>Recent files</strong> popup. Open it via ${
          action("RecentFiles")
        } and select what you need using the arrow keys on the keyboard.")
    }
    task {
      triggers("Switcher")
      text(
        "The second is <strong>Switcher</strong>, which looks the same but immediately disappears after you select something. Hold the <shortcut>⌃</shortcut> key while pressing ${
          action("Switcher")
        }, and press it again to switch to select some file or toolwindow. Release the <shortcut>⌃</shortcut> key after you've selected what you wanted.")
    }


  }
}