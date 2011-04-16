package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssEmbedImageDeclaration;
import com.intellij.flex.uiDesigner.css.CssEmbedSwfDeclaration;

public class AssetLoadSemaphore {
  private var images:Vector.<int> = new Vector.<int>(8);
  private var swfs:Vector.<int> = new Vector.<int>(8);

  private var imageCounter:int;
  private var swfCounter:int;

  public function notifyEmbedSwf(cssDeclaration:CssEmbedSwfDeclaration):void {
    swfs[swfCounter++] = cssDeclaration.id;
  }

  public function notifyEmbedImage(cssDeclaration:CssEmbedImageDeclaration):void {
    images[imageCounter++] = cssDeclaration.id;
  }
}
}
