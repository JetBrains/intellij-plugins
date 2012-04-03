package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.AbstractDocumentDisplayManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Shape;
import flash.display.Sprite;
import flash.errors.IllegalOperationError;
import flash.geom.Point;

import flash.geom.Rectangle;
import flash.geom.Transform;
import flash.utils.Dictionary;

[Abstract]
internal class FlexDocumentDisplayManagerBase extends AbstractDocumentDisplayManager {
  // offset due: 0 child of system manager is application
  internal static const OFFSET:int = 1;

  protected static const INITIALIZE_ERROR_EVENT_TYPE:String = "initializeError";

  protected static const LAYOUT_MANAGER_FQN:String = "mx.managers::ILayoutManager";
  protected static const POP_UP_MANAGER_FQN:String = "mx.managers::IPopUpManager";
  protected static const TOOL_TIP_MANAGER_FQN:String = "mx.managers::IToolTipManager2";

  protected static const skippedEvents:Dictionary = new Dictionary();
  skippedEvents.cursorManagerRequest = true;
  skippedEvents.dragManagerRequest = true;
  skippedEvents.initManagerRequest = true;
  skippedEvents.systemManagerRequest = true;
  skippedEvents.tooltipManagerRequest = true;

  protected const implementations:Dictionary = new Dictionary();

  protected var proxiedListeners:Dictionary;
  protected var proxiedListenersInCapture:Dictionary;

  public function registerImplementation(interfaceName:String, impl:Object):void {
    throw new Error("");
  }

  private static var fakeTransform:Transform;

  override public function get transform():Transform {
    if (fakeTransform == null) {
      fakeTransform = new Transform(new Shape());
    }
    return fakeTransform;
  }

  public function get preloadedRSLs():Dictionary {
    return null;
  }

  public function allowDomain(...rest):void {
  }

  public function allowInsecureDomain(...rest):void {
  }

  public function get isProxy():Boolean {
    // must be false, otherwise UIComponet will ignore our specified actual size
    // see validateDisplayList
    return false;
  }

  public function isTopLevel():Boolean {
    return true;
  }

  public function isTopLevelRoot():Boolean {
    return true;
  }

  public function getTopLevelRoot():DisplayObject {
    return this;
  }

  public function getSandboxRoot():DisplayObject {
    return this;
  }

  public function get embeddedFontList():Object {
    return null;
  }

  public function get focusPane():Sprite {
    return null;
  }

  public function set focusPane(value:Sprite):void {
  }

  public function deployMouseShields(deploy:Boolean):void {
  }

  public function invalidateParentSizeAndDisplayList():void {
  }

  override public function get parent():DisplayObjectContainer {
    return null;
  }

  public function get numModalWindows():int {
    return 0;
  }

  public function set numModalWindows(value:int):void {
  }

  public function set document(value:Object):void {
    throw new Error("forbidden");
  }

  // The index of the highest child that is a cursor
  private var _cursorIndex:int = 0;
  internal function get cursorIndex():int {
    return _cursorIndex;
  }

  internal function set cursorIndex(value:int):void {
    _cursorIndex = value;
  }

  private var _screen:Rectangle;
  public function get screen():Rectangle {
    if (_screen == null) {
      _screen = new Rectangle();
    }

    _screen.width = super.parent.width;
    _screen.height = super.parent.height;
    return _screen;
  }

  override public function setChildIndex(child:DisplayObject, index:int):void {
    super.setChildIndex(child, OFFSET + index);
  }

  public function $setChildIndex(child:DisplayObject, index:int):void {
    super.setChildIndex(child, index);
  }

  override public function getChildIndex(child:DisplayObject):int {
    return super.getChildIndex(child) - OFFSET;
  }

  internal function $getChildIndex(child:DisplayObject):int {
    return super.getChildIndex(child);
  }

  override public function addChild(child:DisplayObject):DisplayObject {
    var addIndex:int = numChildren;
    if (child.parent == this) {
      addIndex--;
    }

    return addChildAt(child, addIndex);
  }

  internal function $contains(child:DisplayObject):Boolean {
    return super.contains(child);
  }

  internal var _toolTipIndex:int = 1; // see comment for _noTopMostIndex init value
  internal function get toolTipIndex():int {
    return _toolTipIndex;
  }

  internal function set toolTipIndex(value:int):void {
    var delta:int = value - _toolTipIndex;
    _toolTipIndex = value;
    cursorIndex += delta;
  }

  internal var _topMostIndex:int;
  internal function get topMostIndex():int {
    return _topMostIndex;
  }

  internal function set topMostIndex(value:int):void {
    var delta:int = value - _topMostIndex;
    _topMostIndex = value;
    toolTipIndex += delta;
  }

  internal var _noTopMostIndex:int = 1; // flex sdk preloader set it as 1 for mouse catcher (missed in our case) and 2 as app (we add app directly)
  internal function get noTopMostIndex():int {
    return _noTopMostIndex;
  }

  internal function set noTopMostIndex(value:int):void {
    var delta:int = value - _noTopMostIndex;
    _noTopMostIndex = value;
    topMostIndex += delta;
  }

  override public function get numChildren():int {
    return noTopMostIndex - OFFSET;
  }

  internal function get $numChildren():int {
    return super.numChildren;
  }

  override public function removeChild(child:DisplayObject):DisplayObject {
    _noTopMostIndex--;
    return removeRawChild(child);
  }

  override public function removeChildAt(index:int):DisplayObject {
    _noTopMostIndex--;
    return $removeChildAt(index + OFFSET);
  }

  [Abstract]
  internal function removeRawChild(child:DisplayObject):DisplayObject {
    throw new IllegalOperationError("abstract");
  }

  internal function $removeChildAt(index:int):DisplayObject {
    return removeRawChild(super.getChildAt(index));
  }

  internal function $getChildAt(index:int):DisplayObject {
    return super.getChildAt(index);
  }

  override public function getObjectsUnderPoint(point:Point):Array {
    var children:Array = [];
    // Get all the children that aren't tooltips and cursors.
    var n:int = _topMostIndex;
    for (var i:int = 0; i < n; i++) {
      var child:DisplayObject = super.getChildAt(i);
      if (child is DisplayObjectContainer) {
        var temp:Array = DisplayObjectContainer(child).getObjectsUnderPoint(point);
        if (temp != null) {
          children = children.concat(temp);
        }
      }
    }

    return children;
  }

  internal function $getObjectsUnderPoint(point:Point):Array {
    return super.getObjectsUnderPoint(point);
  }
}
}
