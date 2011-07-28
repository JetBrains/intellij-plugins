package com.intellij.flex.uiDesigner.plugins.test {
import avmplus.HIDE_NSURI_METHODS;
import avmplus.HIDE_OBJECT;
import avmplus.INCLUDE_METADATA;
import avmplus.INCLUDE_METHODS;
import avmplus.INCLUDE_TRAITS;
import avmplus.USE_ITRAITS;
import avmplus.describe;

import com.intellij.flex.uiDesigner.DocumentManager;
import com.intellij.flex.uiDesigner.ProjectManager;
import com.intellij.flex.uiDesigner.SocketDataHandler;
import com.intellij.flex.uiDesigner.flex.SystemManagerSB;

import flash.display.NativeWindow;
import flash.events.Event;
import flash.events.IEventDispatcher;
import flash.events.TimerEvent;
import flash.geom.Point;
import flash.net.Socket;
import flash.utils.Dictionary;
import flash.utils.IDataInput;
import flash.utils.Timer;
import flash.utils.getQualifiedClassName;

public class TestSocketDataHandler implements SocketDataHandler {
  public static const CLASS:int = 1;
  
  private static const c:Vector.<Class> = new <Class>[MxmlTest, StatesTest, InjectedASTest, AppTest, StyleTest, UITest];
  private const describeCache:Dictionary = new Dictionary();

  private var projectManager:ProjectManager;
  private var timeoutTimer:Timer;

  public function TestSocketDataHandler(projectManager:ProjectManager) {
    this.projectManager = projectManager;
  }
  
  private var _socket:Socket;
  public function set socket(value:Socket):void {
    _socket = value;
  }
  
  private static function collectTestAnnotation(clazz:Class):Dictionary {
    var methodInfo:Dictionary = new Dictionary();
    var methods:Array = describe(clazz, INCLUDE_METHODS | INCLUDE_METADATA | HIDE_OBJECT | HIDE_NSURI_METHODS | INCLUDE_TRAITS | USE_ITRAITS).traits.methods;
    var className:String = getQualifiedClassName(clazz);
    for each (var method:Object in methods) {
      if (method.metadata != null && method.declaredBy == className) {
        var testAnnotation:TestAnnotation = null;
        for each (var annotation:Object in method.metadata) {
          if (annotation.name == "Test") {
            if (testAnnotation == null) {
              testAnnotation = new TestAnnotation();
            }
            
            for each (var entry:Object in annotation.value) {
              if (entry.key == "") {
                testAnnotation[entry.value] = true;
              }
              else {
                testAnnotation[entry.key] = entry.value;
              }
            }
          }
        }

        if (testAnnotation != null) {
          methodInfo[method.name] = testAnnotation;
        }
      }
    }
    
    return methodInfo;
  }

  public function handleSockedData(messageSize:int, methodNameSize:int, data:IDataInput):void {
    var method:String = data.readUTFBytes(methodNameSize);

    var clazz:Class = c[data.readByte()];

    var methodInfo:Dictionary = describeCache[clazz];
    if (methodInfo == null) {
      methodInfo = collectTestAnnotation(clazz);
      describeCache[clazz] = methodInfo;
    }
    var testAnnotation:TestAnnotation = methodInfo[method] || TestAnnotation.DEFAULT;
    var documentManager:DocumentManager = projectManager.project == null ? null : DocumentManager(projectManager.project.getComponent(DocumentManager));
    if (!testAnnotation.nullableDocument && documentManager != null && documentManager.document == null) {
      trace("wait document");
      IEventDispatcher(documentManager).addEventListener("documentChanged", function(event:Event):void {
        IEventDispatcher(event.currentTarget).removeEventListener(event.type, arguments.callee);
        testOnSystemManagerReady(documentManager, clazz, method, testAnnotation);
      });
    }
    else {
      testOnSystemManagerReady(documentManager, clazz, method, testAnnotation);
    }
  }

  private function testOnSystemManagerReady(documentManager:DocumentManager, clazz:Class, method:String, testAnnotation:TestAnnotation):void {
    // todo investigate, is it a problem for real code
    // (components in user document can call systemManager.addEventListener, but our systemManager requires stage at this moment)?
    var systemManager:SystemManagerSB = testAnnotation.nullableDocument ? null : documentManager.document.systemManager;
    if (systemManager != null && systemManager.stage == null) {
      systemManager.addRealEventListener(Event.ADDED_TO_STAGE, function(event:Event):void {
        IEventDispatcher(event.currentTarget).removeEventListener(event.type, arguments.callee);
        test(clazz, method, testAnnotation);
      });
    }
    else {
      test(clazz, method, testAnnotation);
    }
  }
  
  private function test(clazz:Class, method:String, testAnnotation:TestAnnotation):void {
    trace("execute test " + method);

    if (clazz == UITest && method == "getStageOffset") {
      getStageOffset(projectManager);
      return;
    }
    
    var test:TestCase = new clazz();
    test.init(projectManager, _socket);
    test.setUp();

    if (testAnnotation.async) {
      if (timeoutTimer == null) {
        timeoutTimer = new Timer(5000, 1);
        timeoutTimer.addEventListener(TimerEvent.TIMER, timeOutHandler);
      }

      timeoutTimer.start();
      test.asyncSuccessHandler = asyncSuccessHandler;
    }
    test[method]();

    if (testAnnotation == null || !testAnnotation.async) {
      success();
    }
  }

  private function getStageOffset(projectManager:ProjectManager):void {
    var window:NativeWindow = projectManager.project.window;
    var point:Point = window.globalToScreen(new Point(0, 0));
    _socket.writeByte(TestServerMethod.custom);

    _socket.writeShort(point.x);
    _socket.writeShort(point.y);
  }
  
  private function success():void {
    _socket.writeByte(TestServerMethod.success);
    _socket.writeUTF("__passed__");
    _socket.flush();
  }
  
  private function fail(message:String):void {
    _socket.writeByte(TestServerMethod.fail);
    _socket.writeUTF(message);
    _socket.flush();
  }

  private function timeOutHandler(event:Event):void {
    fail("time out");
  }
  
  private function asyncSuccessHandler():void {
    timeoutTimer.reset();
    success();
  }

  public function pendingReadIsAllowable(method:int):Boolean {
    return false;
  }
}
}

class TestAnnotation {
  public static const DEFAULT:TestAnnotation = new TestAnnotation();

  public var async:Boolean;
  public var nullableDocument:Boolean;
}

final class TestServerMethod {
  public static const success:int = 100;
  public static const fail:int = 101;
  public static const custom:int = 102;
}