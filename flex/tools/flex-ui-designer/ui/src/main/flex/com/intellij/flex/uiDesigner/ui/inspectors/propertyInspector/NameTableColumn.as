package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.Insets;
import cocoa.tableView.TableView;
import cocoa.tableView.TextTableColumn;
import cocoa.text.TextLineRendererFactory;

import flash.text.engine.TextLine;

public class NameTableColumn extends TextTableColumn {
  public function NameTableColumn(dataField:String, rendererFactory:TextLineRendererFactory, tableView:TableView, textInsets:Insets) {
    super(dataField, rendererFactory, tableView, textInsets);
  }

  override protected function createTextLine(rowIndex:int):TextLine {
    var description:Object = tableView.dataSource.getObjectValue(this, rowIndex);
    if (!("editable" in description)) {
      prepareDescription(description);
    }

    return textLineRendererFactory.create(description.name, actualWidth, description.editable ? null : ValueTableColumn.stringDisabled);
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

    description.editable = !readOnly;
  }
}
}
