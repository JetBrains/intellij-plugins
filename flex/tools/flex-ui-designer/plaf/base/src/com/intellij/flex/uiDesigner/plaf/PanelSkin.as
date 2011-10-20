package com.intellij.flex.uiDesigner.plaf {
import cocoa.Border;
import cocoa.Component;
import cocoa.IconButton;
import cocoa.LabelHelper;
import cocoa.Panel;
import cocoa.Toolbar;
import cocoa.View;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.LookAndFeelProvider;
import cocoa.plaf.TextFormatId;
import cocoa.plaf.WindowSkin;
import cocoa.plaf.aqua.AquaLookAndFeel;
import cocoa.plaf.basic.AbstractSkin;

import flash.display.DisplayObject;
import flash.display.Graphics;

import spark.components.supportClasses.GroupBase;
import spark.layouts.VerticalLayout;

public class PanelSkin extends AbstractSkin implements WindowSkin {
  private var labelHelper:LabelHelper;

  private var titleBorder:Border;
  private var contentBorder:Border;

  private var statusText:StatusText;

//  private var minimizeButton:IconButton;
//  private var closeSideButton:IconButton;

  public function PanelSkin() {
    labelHelper = new LabelHelper(this);
  }

  public function set title(value:String):void {
    labelHelper.text = value;
  }

  private var _toolbar:Toolbar;
  public function set toolbar(value:Toolbar):void {
    _toolbar = value;
  }

  private var _contentView:View;
  public function set contentView(value:View):void {
    _contentView = value;
  }

  override public function attach(component:Component, laf:LookAndFeel):void {
    var panelLaF:LookAndFeel = AquaLookAndFeel(laf).createPanelLookAndFeel();
    LookAndFeelProvider(component).laf = panelLaF;
    super.attach(component, panelLaF);
  }

  override protected function createChildren():void {
    super.createChildren();

    titleBorder = getBorder("title.b");
    contentBorder = getBorder("b");

    if (_toolbar != null) {
      var toolbarSkin:DisplayObject = DisplayObject(_toolbar.createView(laf));
      toolbarSkin.height = 20;
      toolbarSkin.x = 8;
      toolbarSkin.y = 20;
      addChild(toolbarSkin);
    }

    _contentView.x = contentBorder.contentInsets.left;
    _contentView.y = titleBorder.layoutHeight;
    if (_contentView is GroupBase && GroupBase(_contentView).layout == null) {
      var layout:VerticalLayout = new VerticalLayout();
      layout.gap = 2;
      GroupBase(_contentView).layout = layout;
    }
    addChild(DisplayObject(_contentView));

    labelHelper.textFormat = laf.getTextFormat(TextFormatId.SYSTEM_HIGHLIGHTED);

//    minimizeButton = createControlButton("minimize", "minimizeButton");
//    closeSideButton = createControlButton("closeSide", "closeSideButton");
  }

  private function createControlButton(iconKey:String, partKey:String):IconButton {
    var iconButton:IconButton = new IconButton();
    iconButton.icon = getIcon(iconKey);

    var iconSkin:DisplayObject = DisplayObject(iconButton.createView(laf));
    component.uiPartAdded(partKey, iconButton);

    iconSkin.y = 3;
    iconSkin.width = 17;
    iconSkin.height = 17;
    addChild(iconSkin);

    return iconButton;
  }

  override protected function measure():void {
    measuredMinWidth = _contentView.minWidth + contentBorder.contentInsets.width;
    measuredWidth = _contentView.getExplicitOrMeasuredWidth() + contentBorder.contentInsets.width;

    var chromeH:Number = contentBorder.contentInsets.height;
    measuredMinHeight = _contentView.minHeight + chromeH;
    measuredHeight = _contentView.getExplicitOrMeasuredHeight() + chromeH;
  }

  // todo find normal way
  public function get contentWidth():Number {
    return (parent.width - getBorder("b").contentInsets.width);
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    var g:Graphics = graphics;
    g.clear();

    contentBorder.draw(g, w, h);
    g.lineStyle();

    var panel:Panel = Panel(component);
    const empty:Boolean = panel.emptyText != null;
    if (empty) {
      if (statusText == null) {
        statusText = new StatusText(this, laf.getBorder("StatusText.b"), laf.getTextFormat("StatusBar.f"));
      }

      statusText.show(g, panel.emptyText, contentBorder.contentInsets, w, h);
    }
    else if (statusText != null) {
      statusText.hide();
    }

    _contentView.visible = !empty;

    g.lineStyle();
    titleBorder.draw(g, w, titleBorder.layoutHeight);

    labelHelper.validate();
    labelHelper.move(3, 13);

    var contentWidth:Number = w - contentBorder.contentInsets.width;
    if (_toolbar != null) {
      _toolbar.skin.setActualSize(contentWidth, 20);
    }
    if (!empty) {
      _contentView.setActualSize(contentWidth, h - contentBorder.contentInsets.height);
    }

//    DisplayObject(minimizeButton.skin).x = w - (17 * 2) - 1 - 3;
//    DisplayObject(closeSideButton.skin).x = w - 17 - 3;
  }
}
}