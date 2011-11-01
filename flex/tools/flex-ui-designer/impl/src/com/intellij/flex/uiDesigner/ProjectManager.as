package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.libraries.LibraryManager;

import flash.desktop.NativeApplication;
import flash.events.Event;
import flash.events.NativeWindowBoundsEvent;

import org.jetbrains.actionSystem.DataManager;

public class ProjectManager {
  private const openProjects:Vector.<Project> = new Vector.<Project>(2);

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
    if (id >= openProjects.length) {
      openProjects.length = Math.max(openProjects.length, id) + 2;
    }
    else {
      assert(openProjects[id] == null);
    }

    openProjects[id] = project;

    addNativeWindowListeners(window);
    window.title = project.name;
    project.window = window;

    DataManager.instance.registerDataContext(window.stage, new ProjectDataContext(project));
  }

  protected function addNativeWindowListeners(window:DocumentWindow):void {
    window.addEventListener(Event.CLOSING, closeHandler);
    window.addEventListener(NativeWindowBoundsEvent.RESIZE, resizeOrMoveHandler);
    window.addEventListener(NativeWindowBoundsEvent.MOVE, resizeOrMoveHandler);
  }

  protected function removeNativeWindowListeners(window:DocumentWindow):void {
    window.removeEventListener(Event.CLOSING, closeHandler);
    window.removeEventListener(NativeWindowBoundsEvent.RESIZE, resizeOrMoveHandler);
    window.removeEventListener(NativeWindowBoundsEvent.MOVE, resizeOrMoveHandler);
  }
  
  public function close(id:int):void {
    var project:Project = openProjects[id];
    assert(project != null);

    DataManager.instance.unregisterDataContext(project.window.stage);
    if (project.window != null) {
      removeNativeWindowListeners(project.window);
      project.window.close();
    }

    closeProject2(id, project);
  }

  public function getById(id:int):Project {
    return openProjects[id];
  }

  private function resizeOrMoveHandler(event:Event):void {
    var window:DocumentWindow = DocumentWindow(event.target);
    for (var i:int = 0, n:int = openProjects.length; i < n; i++) {
      var project:Project = openProjects[i];
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
    for (var i:int = 0, n:int = openProjects.length; i < n; i++) {
      var project:Project = openProjects[i];
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
    openProjects[id] = null;

    moduleManager.remove(project, function (module:Module):void {
      libraryManager.remove(module.librarySets);
    });
  }

  //noinspection JSUnusedLocalSymbols
  private function getLastProject():Project {
    var project:Project;
    var n:int = openProjects.length;
    while (n > 0) {
      if ((project = openProjects[--n]) != null) {
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
import org.jetbrains.actionSystem.DataKey;

final class ProjectDataContext implements DataContext {
  private var project:Project;

  public function ProjectDataContext(project:Project) {
    this.project = project;
  }

  public function getData(dataKey:DataKey):Object {
    switch (dataKey) {
      case PlatformDataKeys.PROJECT:
        return project;

      case PlatformDataKeys.DOCUMENT:
        return DocumentManager(project.getComponent(DocumentManager)).document;

      case PlatformDataKeys.ELEMENT:
        return ElementManager(project.getComponent(ElementManager)).element;

      default:
        return null;
    }
  }
}