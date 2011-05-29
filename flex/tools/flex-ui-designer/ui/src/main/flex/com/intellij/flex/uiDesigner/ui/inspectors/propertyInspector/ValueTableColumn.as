package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.Insets;
import cocoa.tableView.TableView;
import cocoa.tableView.TextLineLinkedList;
import cocoa.tableView.TextLineLinkedListEntry;
import cocoa.tableView.TextTableColumn;
import cocoa.text.TextLineRendererFactory;
import cocoa.util.StringUtil;

import flash.display.DisplayObject;
import flash.text.engine.ElementFormat;
import flash.text.engine.FontDescription;
import flash.text.engine.FontPosture;
import flash.text.engine.FontWeight;
import flash.text.engine.TextLine;

public class ValueTableColumn extends TextTableColumn {
  private static const FONT_DESCRIPTION:FontDescription = new FontDescription("Monaco, Consolas");
  private static const FONT_DESCRIPTION_ITALIC:FontDescription = new FontDescription("Monaco, Consolas", FontWeight.NORMAL, FontPosture.ITALIC);
  private static const identifier:ElementFormat = new ElementFormat(FONT_DESCRIPTION, 11, 0x000080);
  private static const func:ElementFormat = identifier;
  private static const numberFormat:ElementFormat = new ElementFormat(FONT_DESCRIPTION, 11, 0x0000ff);
  private static const staticField:ElementFormat = new ElementFormat(FONT_DESCRIPTION_ITALIC, 11, 0x660e7a);

  private var dataSource:MyTableViewDataSource;

  public function ValueTableColumn(rendererFactory:TextLineRendererFactory, tableView:TableView, textInsets:Insets) {
    super(null, rendererFactory, tableView, textInsets);

    dataSource = MyTableViewDataSource(tableView.dataSource);
  }

  override public function createAndLayoutRenderer(rowIndex:int, x:Number, y:Number):DisplayObject {
    var description:Object = dataSource.getObjectValue(this, rowIndex);
    var type:String = description.type;
    var object:Object = dataSource.object;
    var text:String;
    var customElementFormat:ElementFormat;
    var newEntry:TextLineLinkedListEntry;
    switch (type) {
      case "int":
      case "uint":
        text = object[description.name];
        customElementFormat = numberFormat;
        break;

      case "String":
        text = object[description.name];
        if (text == null) {
          text = "null";
          customElementFormat = func;
        }
        else if (text.length == 0) {
          newEntry = cells.create(null);
        }
        break;

      case "Number":
        var number:Number = Number(object[description.name]);
        if (number != number) {
          text = "NaN";
          customElementFormat = staticField;
        }
        else {
          text = number.toString();
          customElementFormat = numberFormat;
        }
        break;

      case "Array":
        text = "[…]";
        break;

      default:
        var v:* = object[description.name];
        if (type == "*" && v === undefined) {
          text = "undefined";
          customElementFormat = staticField;
        }
        else if (type == null) {
          text = "null";
          customElementFormat = func;
        }
        else {
          text = StringUtil.startsWith(type, "__AS3__.vec::Vector.<") ? "<>[…]" : "{…}";
        }
    }

    var line:TextLine;
    if (newEntry == null) {
      line = textLineRendererFactory.create(text, actualWidth, customElementFormat);
      newEntry = cells.create(line);

      line.x = x + textInsets.left;
      line.y = y + tableView.rowHeight - textInsets.bottom;
    }

    if (previousEntry == null) {
      cells.addFirst(newEntry);
    }
    else {
      cells.addAfter(previousEntry, newEntry);
    }
    previousEntry = newEntry;


    return line;
  }
}
}
