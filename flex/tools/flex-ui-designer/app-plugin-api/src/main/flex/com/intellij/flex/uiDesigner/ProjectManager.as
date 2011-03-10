package com.intellij.flex.uiDesigner {
import flash.utils.Dictionary;

public class ProjectManager {
  private const idMap:Dictionary = new Dictionary();

  public function open(id:int, project:Project):void {
    assert(!(id in idMap));
    idMap[id] = project;
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