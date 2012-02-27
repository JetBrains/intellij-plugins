package com.intellij.flex.uiDesigner {
import avmplus.HIDE_NSURI_METHODS;
import avmplus.HIDE_OBJECT;
import avmplus.INCLUDE_METADATA;
import avmplus.INCLUDE_METHODS;
import avmplus.INCLUDE_TRAITS;
import avmplus.USE_ITRAITS;
import avmplus.describe;

import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.desktop.NativeApplication;
import flash.display.NativeWindow;
import flash.events.Event;
import flash.events.TimerEvent;
import flash.geom.Point;
import flash.net.Socket;
import flash.utils.Dictionary;
import flash.utils.IDataInput;
import flash.utils.Timer;
import flash.utils.getQualifiedClassName;

import org.jetbrains.actionSystem.DataManager;

internal class TestSocketDataHandler implements SocketDataHandler {
  public static const CLASS:int = 1;

  private static const GET_STAGE_OFFSET:int = 120;
  
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

  public function handleSockedData(messageSize:int, classId:int, callbackId:int, input:IDataInput):void {
    const moduleId:int = input.readShort();
    const module:Module = moduleId == -1 ? null : moduleManager.getById(moduleId);
    const project:Project = module == null ? null : module.project;
    var testTask:TestTask;
    var clazz:Class;

    switch (classId) {
      case GET_STAGE_OFFSET:
        getStageOffset(project.window);
        return;

      default:
        clazz = c[classId];
        break;
    }

    const method:String = AmfUtil.readString(input);

    var methodInfo:Dictionary;
    var testAnnotation:TestAnnotation;
    if (clazz != null) {
      methodInfo = describeCache[clazz];
      if (methodInfo == null) {
        methodInfo = collectTestAnnotation(clazz);
        describeCache[clazz] = methodInfo;
      }

      testAnnotation = methodInfo[method] || TestAnnotation.DEFAULT;
    }
    else {
      testAnnotation = TestAnnotation.DEFAULT;
    }

    var documentManager:DocumentManager = project == null ? null : DocumentManager(project.getComponent(DocumentManager));
    const testDocumentFilename:String = (testAnnotation.document == null ? method : testAnnotation.document) + ".mxml";

    if (testTask == null) {
      testTask = new TestTask();
    }
    testTask.init(project, documentManager, clazz, method, testAnnotation);

    if (!testAnnotation.nullableDocument && documentManager != null &&
        (documentManager.document == null || documentManager.document.documentFactory.file.name != testDocumentFilename)) {
      trace("wait document");
      documentManager.documentChanged.addOnce(function():void {
        testOnDocumentRendered(testTask);
      });
    }
    else {
      testOnDocumentRendered(testTask);
    }
  }

  private function testOnDocumentRendered(testTask:TestTask):void {
    test(testTask.project, testTask.clazz, testTask.method, testTask.testAnnotation);
  }
  
  private function test(project:Project, clazz:Class, method:String, testAnnotation:TestAnnotation):void {
    trace("execute test " + method);
    var test:TestCase = new clazz();
    test.init(project == null ? new EmptyDataContext() : DataManager.instance.getDataContext(project.window.stage.getChildAt(0)), _socket);
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
    // it is not required for window.globalToScreen, but our java side requires active front window (for java.awt.Robot)
    NativeApplication.nativeApplication.activate(window);

    var point:Point = window.globalToScreen(new Point(0, 0));
    _socket.writeByte(TestServerMethod.custom);

    _socket.writeShort(point.x);
    _socket.writeShort(point.y);
    _socket.flush();
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

import com.intellij.flex.uiDesigner.DocumentManager;
import com.intellij.flex.uiDesigner.Project;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataKey;

class TestTask {
  internal var project:Project;
  internal var documentManager:DocumentManager;
  internal var clazz:Class;
  internal var method:String;
  internal var testAnnotation:TestAnnotation;

  public function init(project:Project, documentManager:DocumentManager, clazz:Class, method:String, testAnnotation:TestAnnotation):void {
    this.project = project;
    this.documentManager = documentManager;
    this.clazz = clazz;
    this.method = method;
    this.testAnnotation = testAnnotation;
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

final class EmptyDataContext implements DataContext {
  public function getData(dataKey:DataKey):Object {
    return null;
  }
}