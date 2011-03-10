package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.DocumentManager;
import com.intellij.flex.uiDesigner.ProjectManager;

import flash.events.Event;
import flash.events.IEventDispatcher;

[Abstract]
internal class BaseTestCase implements TestCase {
  protected var documentManager:DocumentManager;
  protected var projectManager:ProjectManager;

  protected var _asyncSuccessHandler:Function;
  public function set asyncSuccessHandler(value:Function):void {
    _asyncSuccessHandler = value;
  }
  
  public function setUp(projectManager:ProjectManager):void {
    this.projectManager = projectManager;
    
    documentManager = DocumentManager(projectManager.project.plexusContainer.lookup(DocumentManager));
    _app = documentManager.document.uiComponent;
  }
  
  private var _app:Object;
  protected final function get app():Object {
    return _app;
  }
  
  protected final function get appContent():Object {
    return _app.contentGroup;
  }
  
  protected final function getDefinition(name:String):Object {
    return documentManager.document.module.context.getDefinition(name);
  }
  
  protected final function getClass(name:String):Class {
    return Class(getDefinition(name));
  }
  
  protected final function get mxInternal():Namespace {
    return Namespace(getDefinition("mx.core.mx_internal"));
  }
  
  protected final function validateUI():Object {
    return app.validateNow();
  }
  
  protected final function asyncSuccess(event:Event, eventHandler:Function):void {
    IEventDispatcher(event.currentTarget).removeEventListener(event.type, eventHandler);
    _asyncSuccessHandler();
  }
}
}
