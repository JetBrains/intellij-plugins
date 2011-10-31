package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.SpriteAssetInitializer;

import flash.geom.Rectangle;

import mx.core.SpriteAsset;

[Abstract]
public class SpriteLoaderAsset extends SpriteAsset {
  public function SpriteLoaderAsset(myClass:Class, symbolClass:Class, bounds:Rectangle) {
    if (symbolClass == null) {
      SpriteAssetInitializer.addPendingClient(myClass, this);
      graphics.drawRect(0, 0, bounds.width, bounds.height);
    }
    else {
      createContent(symbolClass);
    }

    super();
  }

  public final function symbolClassAvailable(symbolClass:Class):void {
    graphics.clear();
    createContent(symbolClass);
  }

  private function createContent(symbolClass:Class):void {
    addChild(new symbolClass());
  }
}
}