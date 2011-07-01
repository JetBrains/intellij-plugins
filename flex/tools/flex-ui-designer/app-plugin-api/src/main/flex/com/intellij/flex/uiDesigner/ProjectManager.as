package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.libraries.LibraryManager;

import flash.desktop.NativeApplication;
import flash.events.Event;
import flash.events.NativeWindowBoundsEvent;

import org.jetbrains.actionSystem.DataManager;

public class ProjectManager {
  private const items:Vector.<Project> = new Vector.<Project>(2);

  private var libraryManager:LibraryManager;
  private var moduleManager:ModuleManager;

  private var applicationExiting:Boolean;

  public function ProjectManager(libraryManager:LibraryManager, moduleManager:ModuleManager) {
    this.libraryManager = libraryManager;
    this.moduleManager = moduleManager;
    NativeApplication.nativeApplication.addEventListener(Event.EXITING, exitingHandler);
  }

  private function exitingHandler(event:Event):void {
    applicationExiting = true;
  }

  public function open(project:Project, window:DocumentWindow):void {
    var id:int = project.id;
    if (id >= items.length) {
      items.length = Math.max(items.length, id) + 2;
    }
    else {
      assert(items[id] == null);
    }

    items[id] = project;

    addNativeWindowListeners(window);
    window.title = project.name;
    project.window = window;

    DataManager.instance.registerDataContext(window.stage, new ProjectDataContext(project));
  }

  protected function addNativeWindowListeners(window:DocumentWindow):void {
    window.addEventListener(Event.CLOSING, closeHandler);
    window.addEventListener(NativeWindowBoundsEvent.RESIZE, resizeHandler);
  }

  protected function removeNativeWindowListeners(window:DocumentWindow):void {
    window.removeEventListener(Event.CLOSING, closeHandler);
    window.removeEventListener(NativeWindowBoundsEvent.RESIZE, resizeHandler);
  }
  
  public function close(id:int):void {
    var project:Project = items[id];
    assert(project != null);

    DataManager.instance.unregisterDataContext(project.window.stage);
    if (project.window != null) {
      removeNativeWindowListeners(project.window);
      project.window.close();
    }

    closeProject2(id, project);
  }

  public function getById(id:int):Project {
    return items[id];
  }
  
  private var _project:Project;
  public function get project():Project {
    return _project;
  }
  // todo select project window
  public function set project(project:Project):void {
    _project = project;
  }

  private function resizeHandler(event:Event):void {
    var window:DocumentWindow = DocumentWindow(event.target);
    for (var i:int = 0, n:int = items.length; i < n; i++) {
      var project:Project = items[i];
      if (project.window == window) {
        Server.instance.saveProjectWindowBounds(project, window.bounds);
        return;
      }
    }
  }

  private function closeHandler(event:Event):void {
    if (applicationExiting) {
      return;
    }

    var window:DocumentWindow = DocumentWindow(event.target);
    for (var i:int = 0, n:int = items.length; i < n; i++) {
      var project:Project = items[i];
      if (project.window == window) {
        removeNativeWindowListeners(window);
        DataManager.instance.unregisterDataContext(window.stage);
        closeProject2(i, project);
        Server.instance.closeProject(project);
        return;
      }
    }

    throw new Error("project must be for window");
  }

  private function closeProject2(id:int, project:Project):void {
    items[id] = null;

    if (_project == project) {
      this.project = getLastProject();
    }

    moduleManager.remove(project, function (module:Module):void {
      libraryManager.remove(module.librarySets);
    });
  }

  private function getLastProject():Project {
    var project:Project;
    var n:int = items.length;
    while (n > 0) {
      if ((project = items[--n]) != null) {
        break;
      }
    }

    return project;
  }
}
}

import com.intellij.flex.uiDesigner.DocumentManager;
import com.intellij.flex.uiDesigner.ElementManager;
import com.intellij.flex.uiDesigner.PlatformDataKeys;
import com.intellij.flex.uiDesigner.Project;

import org.jetbrains.actionSystem.DataContext;

class ProjectDataContext implements DataContext {
  private var project:Project;

  public function ProjectDataContext(project:Project) {
    this.project = project;
  }

  public function getData(dataId:String):* {
    switch (dataId) {
      case PlatformDataKeys.PROJECT.name:
        return project;

      case PlatformDataKeys.DOCUMENT.name:
        return DocumentManager(project.getComponent(DocumentManager)).document;

      case PlatformDataKeys.ELEMENT.name:
        return ElementManager(project.getComponent(ElementManager)).element;

      default:
        return null;
    }
  }
}