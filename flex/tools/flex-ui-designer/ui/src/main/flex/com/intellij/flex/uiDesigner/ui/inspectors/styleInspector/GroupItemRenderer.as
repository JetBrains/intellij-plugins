package com.intellij.flex.uiDesigner.ui.inspectors.styleInspector {
import com.intellij.flex.uiDesigner.ui.inspectors.AbstractTitledBlockItemRenderer;

import flash.display.DisplayObjectContainer;

import mx.core.IDataRenderer;

public class GroupItemRenderer extends AbstractTitledBlockItemRenderer implements IDataRenderer {
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
}
}