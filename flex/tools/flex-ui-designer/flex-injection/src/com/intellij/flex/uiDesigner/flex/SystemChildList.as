package com.intellij.flex.uiDesigner.flex {
import flash.display.DisplayObject;
import flash.geom.Point;

import mx.core.IChildList;

public class SystemChildList implements IChildList {
  public function SystemChildList(owner:SystemManager, lowerBoundReference:String, upperBoundReference:String) {
    this.owner = owner;
    this.lowerBoundReference = lowerBoundReference;
    this.upperBoundReference = upperBoundReference;
  }

	private var owner:SystemManager;

	private var lowerBoundReference:String;
	private var upperBoundReference:String;

  public function get numChildren():int {
    return owner[upperBoundReference] - owner[lowerBoundReference];
  }

  public function addChild(child:DisplayObject):DisplayObject {
    return owner.addRawChildAt(child, owner[upperBoundReference]++);
  }

  public function addChildAt(child:DisplayObject, index:int):DisplayObject {
    owner.addRawChildAt(child, owner[lowerBoundReference] + index);
    owner[upperBoundReference]++;
    return child;
  }

  public function removeChild(child:DisplayObject):DisplayObject {
    var index:int = owner.$getChildIndex(child);
    if (owner[lowerBoundReference] <= index && index < owner[upperBoundReference]) {
      owner.removeRawChild(child);
      owner[upperBoundReference]--;
    }
    return child;
  }

  public function removeChildAt(index:int):DisplayObject {
    var child:DisplayObject = owner.$removeChildAt(index + owner[lowerBoundReference]);
    owner[upperBoundReference]--;
    return child;
  }

  public function getChildAt(index:int):DisplayObject {
    return owner.$getChildAt(owner[lowerBoundReference] + index);
  }

  public function getChildByName(name:String):DisplayObject {
    return owner.getChildByName(name);
  }

  public function getChildIndex(child:DisplayObject):int {
    return owner.$getChildIndex(child) - owner[lowerBoundReference];
  }

  public function setChildIndex(child:DisplayObject, newIndex:int):void {
    owner.$setChildIndex(child, owner[lowerBoundReference] + newIndex);
  }

  public function getObjectsUnderPoint(point:Point):Array {
    return owner.$getObjectsUnderPoint(point);
  }

  public function contains(child:DisplayObject):Boolean {
    if (child != owner && owner.$contains(child)) {
      while (child.parent != owner) {
        child = child.parent;
      }
      var childIndex:int = owner.$getChildIndex(child);
      if (childIndex >= owner[lowerBoundReference] && childIndex < owner[upperBoundReference]) {
        return true;
      }
    }
    return false;
  }
}
}
