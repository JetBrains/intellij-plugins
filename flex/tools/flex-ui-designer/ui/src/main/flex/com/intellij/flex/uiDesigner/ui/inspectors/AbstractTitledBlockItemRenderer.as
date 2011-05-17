package com.intellij.flex.uiDesigner.ui.inspectors {
import com.intellij.flex.uiDesigner.ui.*;

import cocoa.AbstractView;
import cocoa.Border;
import cocoa.LabelHelper;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.LookAndFeelProvider;
import cocoa.text.TextFormat;

import flash.display.Graphics;

public class AbstractTitledBlockItemRenderer extends AbstractView implements LookAndFeelProvider {
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

  override protected function updateDisplayList(w:Number, h:Number):void {
    if (w == 0) {
      return;
    }

    labelHelper.validate();
    labelHelper.move(5, 14);

    var g:Graphics = graphics;
    g.clear();
    border.draw(this, g, w, border.layoutHeight);
  }
}
}
