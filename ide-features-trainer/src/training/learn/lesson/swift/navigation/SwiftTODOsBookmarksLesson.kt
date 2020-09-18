package training.learn.lesson.swift.navigation

import com.intellij.icons.AllIcons
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class SwiftTODOsBookmarksLesson(module: Module) : KLesson("swift.navigation.bookmarks", "TODOs & Bookmarks", module, "Swift") {

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
      triggers("ActivateTODOToolWindow")
      text(
        "Have you ever wondered how many todos there are in your code? In <ide/>, there is a dedicated <strong>TODO</strong> toolwindow for them. Activate it by using ${
            action("ActivateTODOToolWindow")
        }.")
    }
    task {
      triggers("com.intellij.ide.todo.SetTodoFilterAction$1")
      text("You can define your own custom <strong>TODO</strong> filters. Open the TODO filters dialog with ${
          icon(AllIcons.General.Filter)
      } → <strong>Edit Filters</strong> and then try adding your own filters using RegExps.")
    }
    task { text("Press ${action("EditorEscape")} to return to editor window.") }
    task { caret(20, 9) }
    task {
      triggers("ToggleBookmark")
      text("Now press ${
          action("ToggleBookmark")
      }. This shortcut toggles a bookmark in your code. Bookmarks are saved in the project directory and are useful if you want to point your colleague to a specific place in your code.")
    }
    task {
      triggers("ShowBookmarks")
      text("View all the bookmarks by using the ${action("ShowBookmarks")} shortcut.")
    }
    task {
      triggers("com.intellij.ide.bookmarks.actions.EditBookmarkDescriptionAction")
      text("You can change the name of a bookmark. Press ${action("EditorSplitLine")} and enter a new name for it.")
    }
    task {
      triggers("ActivateFavoritesToolWindow")
      text("Close the <strong>Bookmarks</strong> dialog by using ${action("EditorEscape")}. Press ${
          action("ActivateFavoritesToolWindow")
      }. The <strong>Favorites</strong> tool window you see aggregates bookmarks, breakpoints, and favorites.")
    }


  }
}