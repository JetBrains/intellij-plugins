package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.SpriteAssetInitializer;

import flash.display.Sprite;
import flash.errors.IllegalOperationError;
import flash.geom.Rectangle;

import mx.core.SpriteAsset;

[Abstract]
public class SpriteLoaderAsset extends SpriteAsset {
  public function SpriteLoaderAsset(myClass:Class, bounds:Rectangle) {
    if (swfClass == null) {
      SpriteAssetInitializer.addPendingClient(myClass, this);
      graphics.drawRect(0, 0, bounds.width, bounds.height);
    }
    else {
      createContent();
    }

    super();
  }

  protected function get swfClass():Class {
    return null;
  }

  public function swfClassAvailable():void {
    graphics.clear();
    createContent();
  }

  protected function createContent():void {
    //var c:Class = swfClass;
    //var s:Sprite = new c();
    //addChild(s);
    throw new IllegalOperationError("abstract");
  }
}
}
