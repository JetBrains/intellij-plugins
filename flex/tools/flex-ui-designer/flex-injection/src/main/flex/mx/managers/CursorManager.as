package mx.managers {
import com.intellij.flex.uiDesigner.css.RootStyleManager;

import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.events.Event;
import flash.events.IEventDispatcher;
import flash.events.IOErrorEvent;
import flash.events.ProgressEvent;
import flash.ui.Mouse;
import flash.ui.MouseCursor;

import mx.styles.CSSStyleDeclaration;

public class CursorManager implements ICursorManager {
  public static const NO_CURSOR:int = 0;

  private var nextCursorId:int = 1;

  private const cursors:Vector.<CursorQueueItem> = new Vector.<CursorQueueItem>();
  private const busyCursors:Vector.<int> = new Vector.<int>();

  private var progressSources:Vector.<Object> = new Vector.<Object>();

  private static var _instance:CursorManager;
  public static function getInstance():ICursorManager {
    if (_instance == null) {
      _instance = new CursorManager();
    }

    return _instance;
  }

  public static function get currentCursorID():int {
    return getInstance().currentCursorID;
  }

  public static function set currentCursorID(value:int):void {
    getInstance().currentCursorID = value;
  }

  public static function get currentCursorXOffset():Number {
    return getInstance().currentCursorXOffset;
  }

  public static function set currentCursorXOffset(value:Number):void {
    getInstance().currentCursorXOffset = value;
  }

  public static function get currentCursorYOffset():Number {
    return getInstance().currentCursorYOffset;
  }

  public static function set currentCursorYOffset(value:Number):void {
    getInstance().currentCursorYOffset = value;
  }

  public static function showCursor():void {
    getInstance().showCursor();
  }

  public static function hideCursor():void {
    getInstance().hideCursor();
  }

  public static function setCursor(cursorClass:Class, priority:int = 2, xOffset:Number = 0, yOffset:Number = 0):int {
    return getInstance().setCursor(cursorClass, priority, xOffset, yOffset);
  }

  public static function removeCursor(cursorID:int):void {
    getInstance().removeCursor(cursorID);
  }

  public static function removeAllCursors():void {
    getInstance().removeAllCursors();
  }

  public static function setBusyCursor():void {
    getInstance().setBusyCursor();
  }

  public static function removeBusyCursor():void {
    getInstance().removeBusyCursor();
  }

  private var _currentCursorID:int;
  public function get currentCursorID():int {
    return _currentCursorID;
  }

  public function set currentCursorID(value:int):void {
    _currentCursorID = value;
  }

  private var _currentCursorXOffset:Number = 0;
  public function get currentCursorXOffset():Number {
    return _currentCursorXOffset;
  }
  public function set currentCursorXOffset(value:Number):void {
    _currentCursorXOffset = value;
  }

  private var _currentCursorYOffset:Number = 0;
  public function get currentCursorYOffset():Number {
    return _currentCursorYOffset;
  }
  public function set currentCursorYOffset(value:Number):void {
    _currentCursorYOffset = value;
  }

  public function showCursor():void {
  }

  public function hideCursor():void {
  }

  public function setCursor(cursorClass:Class, priority:int = 2, xOffset:Number = 0, yOffset:Number = 0):int {
    var cursorId:int = nextCursorId++;
    cursors.push(new CursorQueueItem(cursorId, cursorClass, priority, xOffset, yOffset));
    cursors.sort(priorityCompare);
    showCurrentCursor();
    return cursorId;
  }

  private static function priorityCompare(a:CursorQueueItem, b:CursorQueueItem):int {
    if (a.priority < b.priority) {
      return -1;
    }
    else if (a.priority == b.priority) {
      return 0;
    }

    return 1;
  }

  private function showCurrentCursor():void {
    if (cursors.length > 0) {
      var item:CursorQueueItem = cursors[0];
      // If the current cursor has changed...
      if (item.cursorId != currentCursorID) {
        if (item.mouseCursorData.data == null) {
          var currentCursor:DisplayObject = new item.cursorClass();
          var bitmapData:BitmapData = new BitmapData(currentCursor.width, currentCursor.height);
          bitmapData.draw(currentCursor);
          item.mouseCursorData.data = new <BitmapData>[bitmapData];
          
          Mouse.registerCursor(item.cursorId.toString(), item.mouseCursorData);
        }

        currentCursorID = item.cursorId;
        currentCursorXOffset = item.mouseCursorData.hotSpot.x;
        currentCursorYOffset = item.mouseCursorData.hotSpot.y;

        Mouse.cursor = currentCursorID.toString();
      }
    }
    else if (currentCursorID != CursorManager.NO_CURSOR) {
      // There is no cursor in the cursor list to display,
      // so cleanup and restore the system cursor.
      currentCursorID = CursorManager.NO_CURSOR;
      currentCursorXOffset = 0;
      currentCursorYOffset = 0;

      Mouse.cursor = MouseCursor.AUTO;
    }
  }

  public function removeCursor(cursorId:int):void {
    for (var i:int = 0, n:int = cursors.length; i < n; i++) {
      if (cursors[i].cursorId == cursorId) {
        cursors.splice(i, 1);
        Mouse.unregisterCursor(cursorId.toString());
        showCurrentCursor();
        return;
      }
    }
  }

  public function removeAllCursors():void {
    cursors.length = 0;
    showCurrentCursor();
  }

  public function setBusyCursor():void {
    var cursorManagerStyleDeclaration:CSSStyleDeclaration = RootStyleManager.getInstance().getMergedStyleDeclaration("mx.managers.CursorManager");
    var busyCursorClass:Class = cursorManagerStyleDeclaration.getStyle("busyCursor");
    busyCursors[busyCursors.length] = setCursor(busyCursorClass, CursorManagerPriority.LOW);
  }

  public function removeBusyCursor():void {
    if (busyCursors.length > 0) {
      removeCursor(busyCursors.pop());
    }
  }

  public function registerToUseBusyCursor(source:Object):void {
    if (source is IEventDispatcher) {
      source.addEventListener(ProgressEvent.PROGRESS, progressHandler);
      source.addEventListener(Event.COMPLETE, completeHandler);
      source.addEventListener(IOErrorEvent.IO_ERROR, completeHandler);
    }
  }

  public function unRegisterToUseBusyCursor(source:Object):void {
    if (source is IEventDispatcher) {
      source.removeEventListener(ProgressEvent.PROGRESS, progressHandler);
      source.removeEventListener(Event.COMPLETE, completeHandler);
      source.removeEventListener(IOErrorEvent.IO_ERROR, completeHandler);
    }
  }

  private function progressHandler(event:ProgressEvent):void {
    var sourceIndex:int = progressSources.indexOf(event.target);
    if (sourceIndex == -1) {
      progressSources[progressSources.length] = event.target;
      setBusyCursor();
    }
  }

  private function completeHandler(event:Event):void {
    var sourceIndex:int = progressSources.indexOf(event.target);
    if (sourceIndex != -1) {
      progressSources.splice(sourceIndex, 1);
      removeBusyCursor();
    }
  }
}
}

import flash.geom.Point;
import flash.ui.MouseCursorData;

class CursorQueueItem {
  public var cursorId:int;
  public var cursorClass:Class;
  public var priority:int;

  public var mouseCursorData:MouseCursorData;

  public function CursorQueueItem(cursorID:int, cursorClass:Class, priority:int, x:Number, y:Number) {
    this.cursorId = cursorID;
    this.cursorClass = cursorClass;
    this.priority = priority;

    mouseCursorData = new MouseCursorData();
    if (x != 0 || y != 0) {
      mouseCursorData.hotSpot = new Point(x, y);
    }
  }


}

