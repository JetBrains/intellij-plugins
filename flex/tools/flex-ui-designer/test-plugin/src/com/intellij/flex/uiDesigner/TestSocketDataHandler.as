package com.intellij.flex.uiDesigner {
import avmplus.HIDE_NSURI_METHODS;
import avmplus.HIDE_OBJECT;
import avmplus.INCLUDE_METADATA;
import avmplus.INCLUDE_METHODS;
import avmplus.INCLUDE_TRAITS;
import avmplus.USE_ITRAITS;
import avmplus.describe;

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

internal class TestSocketDataHandler implements SocketDataHandler {
  public static const CLASS:int = 1;
  
  private static const c:Vector.<Class> = new <Class>[CommonTest, StatesTest, InjectedASTest, AppTest, StyleTest, UITest, MxTest, MobileTest];
  private const describeCache:Dictionary = new Dictionary();

  private var moduleManager:ModuleManager;
  private var timeoutTimer:Timer;

  public function TestSocketDataHandler(moduleManager:ModuleManager) {
    this.moduleManager = moduleManager;
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

  public function handleSockedData(messageSize:int, methodNameSize:int, input:IDataInput):void {
    const method:String = input.readUTFBytes(methodNameSize);
    var moduleId:int = input.readShort();
    const module:Module = moduleId == -1 ? null : moduleManager.getById(moduleId);
    const clazz:Class = c[input.readByte()];

    const project:Project = module == null ? null : module.project;

    if (clazz == UITest && method == "getStageOffset") {
      getStageOffset(project.window);
      return;
    }

    var methodInfo:Dictionary = describeCache[clazz];
    if (methodInfo == null) {
      methodInfo = collectTestAnnotation(clazz);
      describeCache[clazz] = methodInfo;
    }
    var testAnnotation:TestAnnotation = methodInfo[method] || TestAnnotation.DEFAULT;
    var documentManager:DocumentManager = project == null ? null : DocumentManager(project.getComponent(DocumentManager));
    const testDocumentFilename:String = (testAnnotation.document == null ? method : testAnnotation.document) + ".mxml";
    if (!testAnnotation.nullableDocument && documentManager != null &&
        (documentManager.document == null || documentManager.document.documentFactory.file.name != testDocumentFilename)) {
      trace("wait document");
      documentManager.documentChanged.addOnce(function():void {
        testOnDocumentDisplayManagerReady(project, documentManager, clazz, method, testAnnotation);
      });
    }
    else {
      testOnDocumentDisplayManagerReady(project, documentManager, clazz, method, testAnnotation);
    }
  }

  private function testOnDocumentDisplayManagerReady(project:Project, documentManager:DocumentManager, clazz:Class, method:String,
                                                     testAnnotation:TestAnnotation):void {
    var documentDisplayManager:DocumentDisplayManager = testAnnotation.nullableDocument ? null : documentManager.document.displayManager;
    if (documentDisplayManager != null && documentDisplayManager.stage == null) {
      documentDisplayManager.addRealEventListener(Event.ADDED_TO_STAGE, function(event:Event):void {
        IEventDispatcher(event.currentTarget).removeEventListener(event.type, arguments.callee);
        test(project, clazz, method, testAnnotation);
      });
    }
    else {
      test(project, clazz, method, testAnnotation);
    }
  }
  
  private function test(project:Project, clazz:Class, method:String, testAnnotation:TestAnnotation):void {
    trace("execute test " + method);
    var test:TestCase = new clazz();
    test.init(project, _socket);
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

  private function getStageOffset(window:NativeWindow):void {
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

  public function describeMethod(methodId:int):String {
    return "test";
  }
}
}

class TestAnnotation {
  public static const DEFAULT:TestAnnotation = new TestAnnotation();

  public var async:Boolean;
  public var nullableDocument:Boolean;
  public var document:String;
}

final class TestServerMethod {
  public static const success:int = 100;
  public static const fail:int = 101;
  public static const custom:int = 102;
}