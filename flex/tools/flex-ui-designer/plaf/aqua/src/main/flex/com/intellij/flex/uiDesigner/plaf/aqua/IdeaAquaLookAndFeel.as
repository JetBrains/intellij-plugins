package com.intellij.flex.uiDesigner.plaf.aqua {
import cocoa.plaf.LookAndFeelUtil;
import cocoa.plaf.aqua.HUDPushButtonSkin;
import cocoa.text.TextFormat;

import com.intellij.flex.uiDesigner.ui.CustomTextFormatId;
import com.intellij.flex.uiDesigner.plaf.IdeaLookAndFeel;

import flash.text.engine.ElementFormat;

public class IdeaAquaLookAndFeel extends IdeaLookAndFeel {
  [Embed(source="/assets", mimeType="application/octet-stream")]
  private static var assetsDataClass:Class;

  [Embed(source="/popup.arrows.small.png")]
  private static var smallArrowsClass:Class;

  [Embed(source="/popup.arrows.small.disabled.png")]
  private static var smallDisabledArrowsClass:Class;
  
  private static const SIDE_PANE_GROUP_ITEM_LABEL_FONT:TextFormat = new TextFormat(new ElementFormat(fontBoldDescription, 11, 0x38393b));

  override protected function initialize():void {
    super.initialize();
    
    LookAndFeelUtil.initAssets(data, assetsDataClass);
    assetsDataClass = null;
    
    data[CustomTextFormatId.SIDE_PANE_GROUP_ITEM_LABEL] = SIDE_PANE_GROUP_ITEM_LABEL_FONT;
    data["TabLabel.PushButton"] = HUDPushButtonSkin;

    data["small.arrows"] = smallArrowsClass;
    data["small.arrows.disabled"] = smallDisabledArrowsClass;
    data["StatusBar.f"] = new TextFormat(new ElementFormat(fontDescription, 12, 0xfbfbfb));
  }
}
}