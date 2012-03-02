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
    const documentId:int = input.readShort();
    var document:Document = documentId == -1 ? null :  DocumentFactoryManager.getInstance().getById(documentId).document;
    const module:Module = moduleId == -1 ? (document == null ? null : document.module) : moduleManager.getById(moduleId);
    const project:Project = module == null ? null : module.project;

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

    var test:TestCase = new clazz();
    try {
      test.init(new TestDataContext(document, project == null ? null : DataManager.instance.getDataContext(project.window.stage.getChildAt(0))), _socket);
      executeTest(test, method, testAnnotation, callbackId);
    }
    catch (e:Error) {
      fail(callbackId, TestUncaughtErrorManager.errorToString(e));
    }
    finally {
      test.tearDown();
    }
  }

  private function executeTest(test:TestCase, method:String, testAnnotation:TestAnnotation, callbackId:int):void {
    trace("execute test " + method);
    test.setUp();
    if (testAnnotation.async) {
      if (timeoutTimer == null) {
        timeoutTimer = new Timer(5000, 1);
        timeoutTimer.addEventListener(TimerEvent.TIMER, function ():void {
          fail(callbackId, "time out");
        });
      }

      timeoutTimer.start();
      test.asyncSuccessHandler = function ():void {
        timeoutTimer.reset();
        Server.instance.callback(callbackId);
      };
    }
    test[method]();

    if (testAnnotation == null || !testAnnotation.async) {
      Server.instance.callback(callbackId);
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

  private function fail(callbackId:int, message:String):void {
    Server.instance.callback(callbackId, false, false);
    _socket.writeUTF(message);
    _socket.flush();
  }

  public function describeMethod(methodId:int):String {
    return "test";
  }
}
}

import com.intellij.flex.uiDesigner.Document;
import com.intellij.flex.uiDesigner.PlatformDataKeys;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataKey;

class TestAnnotation {
  public static const DEFAULT:TestAnnotation = new TestAnnotation();

  public var async:Boolean;
}

final class TestServerMethod {
  public static const custom:int = 102;
}

final class TestDataContext implements DataContext {
  private var document:Document;
  private var parent:DataContext;

  public function TestDataContext(document:Document, parent:DataContext) {
    this.document = document;
    this.parent = parent;
  }

  public function getData(dataKey:DataKey):Object {
    switch (dataKey) {
      case PlatformDataKeys.DOCUMENT:
        return document;

      default:
        return parent.getData(dataKey);
    }
  }
}