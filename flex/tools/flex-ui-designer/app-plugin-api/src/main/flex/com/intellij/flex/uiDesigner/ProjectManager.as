package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import flash.desktop.NativeApplication;
import flash.display.NativeWindow;
import flash.events.Event;
import flash.utils.Dictionary;

import org.flyti.plexus.PlexusManager;

public class ProjectManager {
  private const idMap:Dictionary = new Dictionary();

  private var applicationExiting:Boolean;

  public function ProjectManager() {
    NativeApplication.nativeApplication.addEventListener(Event.EXITING, exitingHandler);
  }

  private function exitingHandler(event:Event):void {
    applicationExiting = true;
  }

  public function open(project:Project, window:DocumentWindow):void {
    assert(!(project.id in idMap));
    idMap[project.id] = project;

    addNativeWindowListeners(window);
    window.title = project.name;
    project.window = window;
  }

  protected function addNativeWindowListeners(window:DocumentWindow):void {
    window.nativeWindow.addEventListener(Event.CLOSING, closeHandler);
  }
  
  public function close(id:int):Project {
    var project:Project = idMap[id];
    assert(project != null);
    delete idMap[id];
    
    if (project.window != null) {
      saveProjectWindowBounds(project);
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

  private function getByNativeWindow(nativeWindow:NativeWindow):Project {
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

  private function closeHandler(event:Event):void {
    if (applicationExiting) {

    }

    saveProjectWindowBounds(getByNativeWindow(NativeWindow(event.target)));
  }

  protected function saveProjectWindowBounds(project:Project):void {
    var nativeWindow:NativeWindow = project.window.nativeWindow;
    nativeWindow.removeEventListener(Event.CLOSING, closeHandler);
    Server(PlexusManager.instance.container.lookup(Server)).saveProjectWindowBounds(project, nativeWindow.bounds);
  }
}
}