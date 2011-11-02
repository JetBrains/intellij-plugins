package com.intellij.flex.uiDesigner.flex {
import flash.display.DisplayObject;
import flash.geom.Point;

import mx.core.IChildList;

internal final class SystemRawChildrenList implements IChildList {
  private var owner:FlexDocumentDisplayManager;

  public function SystemRawChildrenList(owner:FlexDocumentDisplayManager) {
    super();

    this.owner = owner;
  }

  public function get numChildren():int {
    return owner.$numChildren;
  }

  public function getChildAt(index:int):DisplayObject {
    return owner.$getChildAt(index);
  }

  public function addChild(child:DisplayObject):DisplayObject {
    return owner.addRawChild(child);
  }

  public function addChildAt(child:DisplayObject, index:int):DisplayObject {
    return owner.addRawChildAt(child, index);
  }

  public function removeChild(child:DisplayObject):DisplayObject {
    return owner.removeRawChild(child);
  }

  public function removeChildAt(index:int):DisplayObject {
    return owner.$removeChildAt(index);
  }

  public function getChildByName(name:String):DisplayObject {
    return owner.getChildByName(name);
  }

  public function getChildIndex(child:DisplayObject):int {
    return owner.$getChildIndex(child);
  }

  public function setChildIndex(child:DisplayObject, newIndex:int):void {
    owner.$setChildIndex(child, newIndex);
  }

  public function getObjectsUnderPoint(point:Point):Array {
    return owner.$getObjectsUnderPoint(point);
  }

  public function contains(child:DisplayObject):Boolean {
    return owner.$contains(child);
  }
}
}
