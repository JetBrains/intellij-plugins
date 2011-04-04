package com.intellij.flex.uiDesigner.flex.cssBlockRenderer {
import cocoa.AbstractView;
import cocoa.Border;
import cocoa.LabelHelper;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.LookAndFeelProvider;
import cocoa.text.TextFormat;

import com.intellij.flex.uiDesigner.plaf.CustomTextFormatId;

import flash.display.DisplayObjectContainer;

import flash.display.Graphics;

import mx.core.IDataRenderer;

public class GroupItemRenderer extends AbstractView implements IDataRenderer, LookAndFeelProvider {
  protected var labelHelper:LabelHelper;
  protected var border:Border;

  private var _laf:LookAndFeel;
  public function get laf():LookAndFeel {
    return _laf;
  }
  public function set laf(value:LookAndFeel):void {
    if (_laf == value) {
      return;
    }

    _laf = value;
    var textFormat:TextFormat = _laf.getTextFormat(CustomTextFormatId.SIDE_PANE_GROUP_ITEM_LABEL);
    if (labelHelper == null) {
      labelHelper = new LabelHelper(this, textFormat);
    }
    else {
      labelHelper.textFormat = textFormat;
    }
    
    border = laf.getBorder("GroupItemRenderer.b");
  }

  private var _data:StyleDeclarationGroupItem;
  public function get data():Object {
    return _data;
  }

  public function set data(value:Object):void {
    if (value == null || value == _data) {
      return;
    }

    _data = StyleDeclarationGroupItem(value);
    var owner:DisplayObjectContainer = _data.owner;
    if (owner == null) {
      labelHelper.text = "Global"
    }
    else {
      var id:String;
      if (!("id" in owner) || (id = owner["id"]) == null) {
        id = owner.name;
      }
      
      labelHelper.text = "Inherited from " + id;
    }

    invalidateDisplayList();
  }

  override protected function measure():void {
    measuredHeight = border.layoutHeight;
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    if (w == 0) {
      return;
    }

    labelHelper.validate();
    labelHelper.move(5, 14);

    var g:Graphics = graphics;
    g.clear();
    border.draw(this, g, w, h);
  }
}
}x§