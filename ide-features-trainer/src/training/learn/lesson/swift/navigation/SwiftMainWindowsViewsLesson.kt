package training.learn.lesson.swift.navigation

import com.intellij.icons.AllIcons
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftMainWindowsViewsLesson(module: Module) : KLesson("swift.navigation.toolwindows", "Main windows & views", module, "Swift") {

  private val sample: LessonSample = parseLessonSample("""
import UIKit

class Navigation: UITableViewController {

    var detailViewController: DetailViewController? = nil
    var objects = [Any]()


    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        navigationItem.leftBarButtonItem = editButtonItem

        let addButton = UIBarButtonItem(barButtonSystemItem: .add, target: self, action: #selector(insertNewObject(_:)))
        navigationItem.rightBarButtonItem = addButton
        if let split = splitViewController {
            let controllers = split.viewControllers
            detailViewController = (controllers[controllers.count-1] as! UINavigationController).topViewController as? DetailViewController
        }
    }

    override func viewWillAppear(_ animated: Bool) {
        clearsSelectionOnViewWillAppear = splitViewController!.isCollapsed
        super.viewWillAppear(animated)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @objc
    func insertNewObject(_ sender: Any) {
        objects.insert(NSDate(), at: 0)
        let indexPath = IndexPath(row: 0, section: 0)
        tableView.insertRows(at: [indexPath], with: .automatic)
    }

    // MARK: - Segues

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "showDetail" {
            if let indexPath = tableView.indexPathForSelectedRow {
                let object = objects[indexPath.row] as! NSDate
                let controller = (segue.destination as! UINavigationController).topViewController as! DetailViewController
                controller.detailItem = object
                controller.navigationItem.leftBarButtonItem = splitViewController?.displayModeButtonItem
                controller.navigationItem.leftItemsSupplementBackButton = true
            }
        }
    }

    // MARK: - Table View

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return objects.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell", for: indexPath)

        let object = objects[indexPath.row] as! NSDate
        cell.textLabel!.text = object.description
        return cell
    }

    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }

    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCell.EditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            objects.remove(at: indexPath.row)
            tableView.deleteRows(at: [indexPath], with: .fade)
        } else if editingStyle == .insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view.
        }
    }


}""".trimIndent())
  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    task {
      text(
        "There are plenty of navigation views in <ide/>. Knowing which one to call in a given situation will help you use the IDE more efficiently.")
    }
    task {
      triggers("ActivateProjectToolWindow")
      text(
        "The <strong>Project</strong> view is similar to the Project navigator in Xcode. It shows all the projects included in a particular workspace, and all the files and groups inside them. Open the <strong>Project</strong> view by pressing (${
          action("ActivateProjectToolWindow")
        }).")
    }
    task {
      text(
        "By default, the <strong>Project</strong> view in <ide/> shows files and folders in the same order as they are stored on the filesystem.")
    }
    task {
      triggers("ProjectView.ManualOrder")
      text("Click the ${
        icon(AllIcons.General.GearPlain)
      } icon in the project view options menu, and then select <strong>Xcode order</strong> to set the same order of files as in Xcode (if you selected Xcode behavior when setting up <ide/>, this option will be enabled automatically).")
    }
    task {
      text(
        "Other features include <strong>Open Files with Single Click</strong> and <strong>Always Select Opened File</strong>. The former allows you to automatically open the code of a file when selecting it, while the latter automatically sets the focus on the file name in the <strong>Project</strong> view when the editor area is in focus.")
    }
    task {
      text(
        "The <strong>Files</strong> view is an additional helpful mode for <ide/>’s <strong>Project</strong> view. It shows all the files inside the directory where ${
          code(".xcworkspace")
        } or ${code(".xcproject")} is located. With this view, you can easily open any file not included in your project and view it.")
    }
    task {
      triggers("com.intellij.ui.content.tabs.TabbedContentAction\$MyNextTabAction")
      text("Activate the <strong>Files</strong> view by pressing ${action("NextTab")}.")
    }
    task { caret(1, 1) }
    task { text("Press ${action("EditorEscape")} to return to the editor window.") }
    task {
      text(
        "The <strong>Structure</strong> view and the <strong>Structure</strong> popup show you the structure of a particular file together with all the ${
          code("//TODO")
        }, ${code("//FIXME")} and ${code("#pragma mark")} or ${
          code("//MARK")
        } comments in your code. They work similarly to the <strong>Symbol</strong> navigator in Xcode.")
    }
    task {
      triggers("ActivateStructureToolWindow")
      text("Activate the <strong>Structure</strong> view by pressing ${action("ActivateStructureToolWindow")}.")
    }
    task {
      text("By using ↑ and ↓ keys, you can select any code construct here and press ${
        action("EditSource")
      } to jump to some place in your code.")
    }
    task {
      triggers("FileStructurePopup")
      text("Now activate the <strong>Structure</strong> popup by pressing ${action("FileStructurePopup")}.")
    }
    task { text("Dismiss this popup using ${action("EditorEscape")} or press ⏎ to jump to some place in your code.") }
    task {
      triggers("FindInPath")
      text("The <strong>Find in Files</strong> (${
        action("FindInPath")
      }) dialog provides the same functionality as <strong>Find</strong> navigator in Xcode. Try using it to run a full-text search now.")
    }
    task {
      triggers("Build")
      text("Let's build our project by invoking ${action("Build")}.")
    }
    task {
      text(
        "The <strong>Build messages</strong> tool window shows compiler output and allows you to filter build messages by their type (see the <strong>Filter messages</strong>(${
          icon(AllIcons.General.Filter)
        }) button on the left-hand side).")
    }
    task {
      triggers("Run")
      text("Now let's run our project on the simulator to see the <strong>Run</strong> tool window. Press ${action("Run")}.")
    }
    task {
      text(
        "This tool window shows the console, where you can view the output of your application (or <strong>Tests runner</strong> if you are running the <strong>Test</strong> Run Configuration). You can always activate it via (${
          action("ActivateRunToolWindow")
        }).")
    }
    task {
      triggers("Stop")
      text("Stop your application by pressing ${action("Stop")}.")
    }
    task {
      triggers("GotoFile", "MasterViewController.swift")
      text("Let's switch back from the emulator window to the IDE and navigate to ${code("MasterViewController.swift")} by pressing ${
        action("GotoFile")
      }.")
    }
    task { caret(11, 9) }
    task {
      triggers("ToggleLineBreakpoint", "Debug")
      text("Toggle a breakpoint at line 11 using ${action("ToggleLineBreakpoint")} and then press ${action("Debug")}.")
    }
    task {
      text("The <strong>Debug</strong> tool window (${
        action("ActivateDebugToolWindow")
      }) is similar to Xcode's <strong>Debug</strong> navigator. It shows all the watches, local variables on the right-hand side, and the list of threads on the left-hand side.")
    }
    task {
      triggers("Stop")
      text("Stop your application by pressing ${action("Stop")}.")
    }
    task { caret(16, 9) }
    task {
      triggers("ViewBreakpoints")
      text(
        "The <strong>Breakpoints</strong> dialog has the same functionality as <strong>Breakpoint</strong> navigator in Xcode - it shows the list of all breakpoints in your project. Activate it by using ${
          action("ViewBreakpoints")
        }.")
    }
    task {
      triggers("ActivateVersionControlToolWindow")
      text("Init the GIT repository via ${
        action("Vcs.QuickListPopupAction")
      }→<strong>Create Git Repository</strong>. Now activate the <strong>VCS</strong> toolwindow with the ${
        action("ActivateVersionControlToolWindow")
      } shortcut.")
    }
    task {
      text(
        "The <strong>VCS</strong> tool window provides everything you need to work with version control systems, including the <strong>Changes</strong> view, the <strong>VCS log</strong>, and more.")
    }


  }
}