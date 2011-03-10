package com.intellij.flex.uiDesigner.plugins.test {
import avmplus.HIDE_NSURI_METHODS;
import avmplus.HIDE_OBJECT;
import avmplus.INCLUDE_METADATA;
import avmplus.INCLUDE_METHODS;
import avmplus.INCLUDE_TRAITS;
import avmplus.USE_ITRAITS;
import avmplus.describe;

import com.intellij.flex.uiDesigner.ProjectManager;
import com.intellij.flex.uiDesigner.SocketDataHandler;

import flash.events.Event;
import flash.events.TimerEvent;
import flash.net.Socket;
import flash.utils.Dictionary;
import flash.utils.IDataInput;
import flash.utils.Timer;
import flash.utils.getQualifiedClassName;

public class TestSocketDataHandler implements SocketDataHandler {
  public static const CLASS:int = 1;
  
  private static const c:Vector.<Class> = new <Class>[MxmlTest, StatesTest, InjectedASTest, AppTest, StyleTest];
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
  
  private function collectTestAnnotation(clazz:Class):Dictionary {
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

  public function handleSockedData(methodNameSize:int, data:IDataInput):void {
    var method:String = data.readUTFBytes(methodNameSize);
    var clazz:Class = c[data.readByte()];
    
    var methodInfo:Dictionary = describeCache[clazz];
    if (methodInfo == null) {
      methodInfo = collectTestAnnotation(clazz);
      describeCache[clazz] = methodInfo;
    }
    
    var test:TestCase = new clazz();
    test.setUp(projectManager);
    
    var testAnnotation:TestAnnotation = methodInfo[method];
    if (testAnnotation != null) {
      if (testAnnotation.async) {
        if (timeoutTimer == null) {
          timeoutTimer = new Timer(5000, 1);
          timeoutTimer.addEventListener(TimerEvent.TIMER, timeOutHandler);
        }
        
        timeoutTimer.start();
        test.asyncSuccessHandler = asyncSuccessHandler;
      }
    }
    test[method]();

    if (testAnnotation == null || !testAnnotation.async) {
      success();
    }
  }
  
  private function success():void {
    _socket.writeByte(1);
    _socket.flush();
  }
  
  private function fail(message:String):void {
    _socket.writeByte(0);
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
  public var async:Boolean;
}
