package org.jetbrains.roboflest {
public function roboflest():void {
  //noinspection BadExpressionStatementJS
  Recorder;
}
}

import flash.desktop.NativeApplication;
import flash.display.NativeWindow;
import flash.display.Stage;
import flash.events.Event;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.filesystem.File;
import flash.filesystem.FileMode;
import flash.filesystem.FileStream;
import flash.geom.Rectangle;
import flash.ui.Keyboard;
import flash.utils.IDataOutput;
import flash.utils.getTimer;

interface Action {
  function save(output:IDataOutput):void;
}

class Recorder {
  new Recorder();
  
  private var recording:Boolean;
  private var actionCounter:int;
  private var actions:Vector.<Action> = new Vector.<Action>(16);
  
  private var lastMouseDownTime:int;
  
  public function Recorder() {
    NativeApplication.nativeApplication.addEventListener(KeyboardEvent.KEY_UP, keyUpHandler);
  }
  
  private function keyUpHandler(event:KeyboardEvent):void {
    if (event.altKey) {
      switch (event.keyCode) {
        case Keyboard.F8:
          recording ? stop() : start();
          recording = !recording;
          break;
        
        case Keyboard.F9:
          actions[actionCounter++] = AssertPlaceholder.instance;
          break;
        
        case Keyboard.F10:
          new ScriptSaver(this);
          break;
      }
    }
  }
  
  public function save(output:IDataOutput):void {
    var i:int;
    while (true) {
      var action:Action = actions[i++];
      action.save(output);
      
      if (i != actionCounter) {
        output.writeUTFBytes("\n");
      }
      else {
        break;
      }
    }
  }
  
  public function clear():void {
    
  }
  
  private function start():void {
    var activeWindow:NativeWindow = NativeApplication.nativeApplication.activeWindow;
    var bounds:Rectangle = activeWindow.bounds;
    if (bounds.width != 1280) {
      bounds.width = 1280;
    }
    if (bounds.height != 760) {
      bounds.height = 770;
    }
    
    activeWindow.bounds = bounds;
    
    var stage:Stage = activeWindow.stage;
    stage.addEventListener(MouseEvent.MOUSE_MOVE, mouseMoveHandler);
    stage.addEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
    stage.addEventListener(MouseEvent.MOUSE_UP, mouseUpHandler);
  }

  private function stop():void {
    var stage:Stage = NativeApplication.nativeApplication.activeWindow.stage;
    stage.removeEventListener(MouseEvent.MOUSE_MOVE, mouseMoveHandler);
    stage.removeEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
    stage.removeEventListener(MouseEvent.MOUSE_UP, mouseUpHandler);
  }

  private function mouseMoveHandler(event:MouseEvent):void {
    if (actionCounter > 0) {
      var lastAction:MouseMove = actions[actionCounter - 1] as MouseMove;
      if (lastAction != null) {
        lastAction.x = event.stageX;
        lastAction.y = event.stageY;
        return;
      }
    }
    
    actions[actionCounter++] = new MouseMove(event.stageX, event.stageY);
  }

  private function mouseDownHandler(event:MouseEvent):void {
    actions[actionCounter++] = MouseDown.instance;
    lastMouseDownTime = getTimer();
  }
  
  private function mouseUpHandler(event:MouseEvent):void {
    var timeAfterDown:int = getTimer() - lastMouseDownTime;
    actions[actionCounter++] = timeAfterDown > 500 ? new MouseUp(timeAfterDown) : MouseUpAsClick.instance;
  }
}

class ScriptSaver {
  private var file:File;
  private var recorder:Recorder;
  
  public function ScriptSaver(recorder:Recorder) {
    this.recorder = recorder;
    
    var usageInfo:File = File.applicationStorageDirectory.resolvePath("recentScript.txt");
    if (usageInfo.exists) {
      var fileStream:FileStream = new FileStream();
      fileStream.open(usageInfo, FileMode.READ);
      file = new File(fileStream.readUTFBytes(fileStream.bytesAvailable));
      fileStream.close();
    }
    
    if (file == null || !file.exists) {
      file = File.userDirectory;
    }
    
    file.addEventListener(Event.SELECT, selectHandler);
    file.addEventListener(Event.CANCEL, cancelHandler);
    file.browseForSave("Save roboflest script");
  }
  
  private function selectHandler(event:Event):void {
    var file:File = File(event.target);
    var fileStream:FileStream = new FileStream();
    fileStream.open(file, FileMode.WRITE);
    recorder.save(fileStream);
    fileStream.close();
    
    var usageInfo:File = File.applicationStorageDirectory.resolvePath("recentScript.txt");
    fileStream.open(usageInfo, FileMode.WRITE);
    fileStream.writeUTFBytes(file.nativePath);
    fileStream.close();
    
    recorder.clear();
  }

  private function cancelHandler(event:Event):void {
    recorder.clear();
  }
}

class MouseMove implements Action {
  public var x:int;
  public var y:int;

  public function MouseMove(x:int, y:int) {
    this.x = x;
    this.y = y;
  }

  public function save(output:IDataOutput):void {
    output.writeUTFBytes("move " + x + " " + y);
  }
}

class MouseDown implements Action {
  public static const instance:MouseDown = new MouseDown();
  
  public function save(output:IDataOutput):void {
    output.writeUTFBytes("down");
  }
}

class MouseUp implements Action {
  
  
  public var timeAfterDown:int;

  public function MouseUp(timeAfterDown:int) {
    this.timeAfterDown = timeAfterDown;
  }
  
  public function save(output:IDataOutput):void {
    output.writeUTFBytes("up " + timeAfterDown);
  }
}

class MouseUpAsClick extends MouseUp {
  public static const instance:MouseUp = new MouseUpAsClick();

  function MouseUpAsClick() {
    super(0);
  }

  override public function save(output:IDataOutput):void {
    output.writeUTFBytes("up");
  }
}

class AssertPlaceholder implements Action {
  public static const instance:AssertPlaceholder = new AssertPlaceholder();
  
  public function save(output:IDataOutput):void {
    output.writeUTFBytes("assert");
  }
}