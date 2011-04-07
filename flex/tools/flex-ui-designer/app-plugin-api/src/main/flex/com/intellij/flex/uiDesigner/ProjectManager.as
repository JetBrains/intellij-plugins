package com.intellij.flex.uiDesigner {
import flash.display.NativeWindow;
import flash.utils.Dictionary;

public class ProjectManager {
  private const idMap:Dictionary = new Dictionary();

  public function open(project:Project):void {
    assert(!(project.id in idMap));
    idMap[project.id] = project;
  }
  
  public function close(id:int):Project {
    var project:Project = idMap[id];
    assert(project != null);
    delete idMap[id];
    
    if (project.window != null) {
      project.window.close();
    }
    if (_project == project) {
      this.project = null;
    }
    
    return project;
  }

  public function getById(id:int):Project {
    return idMap[id];
  }

  public function getByNativeWindow(nativeWindow:NativeWindow):Project {
    for each (var project:Project in idMap) {
      if (project.window.nativeWindow == nativeWindow) {
        return project;
      }
    }

    throw new Error("project must be for window");
  }

  private var _project:Project;
  public function get project():Project {
    return _project;
  }
  // todo select project window
  public function set project(project:Project):void {
    _project = project;
  }
}
}