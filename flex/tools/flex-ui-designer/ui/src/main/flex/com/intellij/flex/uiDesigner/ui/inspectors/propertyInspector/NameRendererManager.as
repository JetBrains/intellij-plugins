package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.Insets;
import cocoa.renderer.TextRendererManager;
import cocoa.text.TextFormat;

import flash.text.engine.TextLine;

public class NameRendererManager extends TextRendererManager {
  public function NameRendererManager(textFormat:TextFormat, textInsets:Insets) {
    super(textFormat, textInsets);
  }

  override protected function createTextLine(itemIndex:int, w:Number):TextLine {
    var description:Object = _dataSource.getObjectValue(itemIndex);
    if (!("editable" in description)) {
      prepareDescription(description);
    }

    return textLineRendererFactory.create(textLineContainer, description.name, w, description.editable ? textFormat.format : ValueRendererManager.stringDisabled);
  }

  private static function prepareDescription(description:Object):void {
    var enumeration:String;
    //var defaultValue:Object;
    var readOnly:Boolean = description.access == "readonly";
    var annotations:Array = description.metadata;
    if (annotations != null && annotations.length != 0) {
      for each (var annotation:Object in annotations) {
        if (annotation.name == "Inspectable") {
          for each (var item:Object in annotation.value) {
            switch (item.key) {
              case "enumeration":
                description.enumeration = item.value;
                break;
              case "environment":
                if (!readOnly && item.value == "none") {
                  readOnly = true;
                }
                break;
              case "defaultValue":
                //defaultValue = item.value;
                break;
            }
          }
          break;
        }
      }
    }

    if (description.name == "uid" && description.declaredBy == "mx.core::UIComponent") {
      readOnly = true;
    }

    description.editable = !readOnly;
  }
}
}
