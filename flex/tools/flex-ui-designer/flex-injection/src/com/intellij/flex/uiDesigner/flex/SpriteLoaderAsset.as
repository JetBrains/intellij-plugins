package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.SpriteAssetInitializer;

import flash.geom.Rectangle;

import mx.core.SpriteAsset;

/**
 * see star.fxg
 * wrapping sprite width: 200
 * shape width 200, but x = 20
 * so, component is cropped. I cannot find any other way except just stupid solution "graphics.drawRect"
 */

[Abstract]
public class SpriteLoaderAsset extends SpriteAsset {
  private var bounds:Rectangle;

  public function SpriteLoaderAsset(myClass:Class, symbolClass:Class, bounds:Rectangle) {
    this.bounds = bounds;

    if (symbolClass == null) {
      SpriteAssetInitializer.addPendingClient(myClass, this);
      graphics.drawRect(0, 0, bounds.right, bounds.bottom);
    }
    else {
      graphics.drawRect(0, 0, bounds.right, bounds.bottom);
      createContent(symbolClass);
    }

    super();
  }

  public final function symbolClassAvailable(symbolClass:Class):void {
    //graphics.clear();
    createContent(symbolClass);
  }

  private function createContent(symbolClass:Class):void {
    addChild(new symbolClass());
  }
}
}