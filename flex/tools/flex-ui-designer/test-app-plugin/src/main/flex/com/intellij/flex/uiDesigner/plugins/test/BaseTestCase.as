package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.DocumentManager;
import com.intellij.flex.uiDesigner.ModuleContextEx;
import com.intellij.flex.uiDesigner.ProjectManager;

import flash.events.Event;
import flash.events.IEventDispatcher;
import flash.net.Socket;

[Abstract]
internal class BaseTestCase implements TestCase {
  protected var documentManager:DocumentManager;
  protected var projectManager:ProjectManager;

  protected var _asyncSuccessHandler:Function;
  public function set asyncSuccessHandler(value:Function):void {
    _asyncSuccessHandler = value;
  }
  
  public function init(projectManager:ProjectManager, socket:Socket):void {
    this.projectManager = projectManager;
  }
  
  public function setUp():void {
    if (projectManager.project != null) {
      documentManager = DocumentManager(projectManager.project.getComponent(DocumentManager));
      if (documentManager.document != null) {
        _app = documentManager.document.uiComponent;
      }
    }
  }
  
  private var _app:Object;
  protected final function get app():Object {
    return _app;
  }
  
  protected final function get appContent():Object {
    return _app.contentGroup;
  }
  
  protected final function getDefinition(name:String):Object {
    var context:ModuleContextEx = documentManager.document.module.context;
    return context.getDefinition(name);
    // compiler bug http://juick.com/develar/1301589
    //return documentManager.document.module.context.getDefinition(name);
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
