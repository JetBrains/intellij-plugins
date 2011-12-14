package com.intellij.flex.uiDesigner.plaf {
import cocoa.Border;
import cocoa.LabelHelper;
import cocoa.Panel;
import cocoa.SkinnableView;
import cocoa.Toolbar;
import cocoa.View;
import cocoa.plaf.TextFormatId;
import cocoa.plaf.WindowSkin;
import cocoa.plaf.aqua.AquaLookAndFeel;
import cocoa.plaf.basic.ContentViewableSkin;

import flash.display.Graphics;

public class PanelSkin extends ContentViewableSkin implements WindowSkin {
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

  override public function attach(component:SkinnableView):void {
    _laf = AquaLookAndFeel(laf).createPanelLookAndFeel();
    Panel(component).laf = _laf;
    super.attach(component);
  }

  override protected function doInit():void {
    super.doInit();

    titleBorder = getBorder("title.b");
    contentBorder = getBorder("b");

    //if (_toolbar != null) {
    //  var toolbarSkin:DisplayObject = DisplayObject(_toolbar.createView(laf));
    //  toolbarSkin.height = 20;
    //  toolbarSkin.x = 8;
    //  toolbarSkin.y = 20;
    //  addChild(toolbarSkin);
    //}

    _contentView.addToSuperview(this, laf, this);
    _contentView.setLocation(contentBorder.contentInsets.left, titleBorder.layoutHeight);

    labelHelper.textFormat = laf.getTextFormat(TextFormatId.SYSTEM_HIGHLIGHTED);

//    minimizeButton = createControlButton("minimize", "minimizeButton");
//    closeSideButton = createControlButton("closeSide", "closeSideButton");
  }

  //private function createControlButton(iconKey:String, partKey:String):IconButton {
  //  var iconButton:IconButton = new IconButton();
  //  iconButton.icon = getIcon(iconKey);
  //
  //  var iconSkin:DisplayObject = DisplayObject(iconButton.createView(laf));
  //  component.uiPartAdded(partKey, iconButton);
  //
  //  iconSkin.y = 3;
  //  iconSkin.width = 17;
  //  iconSkin.height = 17;
  //  addChild(iconSkin);
  //
  //  return iconButton;
  //}

  override public function getMinimumWidth(hHint:int = -1):int {
    return _contentView.getMinimumWidth() + contentBorder.contentInsets.width;
  }

  override public function getMinimumHeight(wHint:int = -1):int {
    return _contentView.getMaximumHeight() + contentBorder.contentInsets.height;
  }

  override public function getPreferredWidth(hHint:int = -1):int {
    return _contentView.getPreferredWidth() + contentBorder.contentInsets.width;
  }

  override public function getPreferredHeight(wHint:int = -1):int {
    return _contentView.getPreferredHeight() + contentBorder.contentInsets.height;
  }

  // todo find normal way
  public function get contentWidth():Number {
    // temp hack
    return (492 - getBorder("b").contentInsets.width);
    //return (parent.width - getBorder("b").contentInsets.width);
  }

  override protected function draw(w:int, h:int):void {
    var g:Graphics = graphics;
    g.clear();

    contentBorder.draw(g, w, h);
    g.lineStyle();

    var panel:Panel = Panel(component);
    const empty:Boolean = panel.emptyText != null;
    if (empty) {
      if (statusText == null) {
        statusText = new StatusText(this, superview.laf.getBorder("StatusText.b"), superview.laf.getTextFormat("StatusText.f"));
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
      _toolbar.setSize(contentWidth, 20);
    }
    if (!empty) {
      _contentView.setSize(contentWidth, h - contentBorder.contentInsets.height);
    }

//    DisplayObject(minimizeButton.skin).x = w - (17 * 2) - 1 - 3;
//    DisplayObject(closeSideButton.skin).x = w - 17 - 3;
  }
}
}