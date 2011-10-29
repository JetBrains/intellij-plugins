package com.intellij.flex.uiDesigner {
import flash.events.Event;
import flash.events.IEventDispatcher;
import flash.net.Socket;

import org.hamcrest.object.HasPropertiesMatcher;

[Abstract]
internal class BaseTestCase implements TestCase {
  protected static const A:String = "A";
  protected static const B:String = "B";

  protected var documentManager:DocumentManager;
  protected var project:Project;

  protected var stateManager:StatesBarManager;

  protected static function l(i:Object):HasPropertiesMatcher {
    return new HasPropertiesMatcher({text: i.toString()});
  }

  protected var _asyncSuccessHandler:Function;
  public function set asyncSuccessHandler(value:Function):void {
    _asyncSuccessHandler = value;
  }
  
  public function init(project:Project, socket:Socket):void {
    this.project = project;
  }
  
  public function setUp():void {
    if (project != null) {
      documentManager = DocumentManager(project.getComponent(DocumentManager));
      if (documentManager.document != null) {
        _app = documentManager.document.uiComponent;
      }

      stateManager = StatesBarManager(project.getComponent(StatesBarManager));
    }
  }

  protected function getMxInternal(propertyName:String):QName {
    return new QName(mxInternal, propertyName);
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

  protected final function setState(name:String):void {
    stateManager.stateName = name;
    validateUI();
  }
}
}
