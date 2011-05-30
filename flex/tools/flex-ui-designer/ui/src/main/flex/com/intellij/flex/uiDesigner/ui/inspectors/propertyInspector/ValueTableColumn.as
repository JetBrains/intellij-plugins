package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.Insets;
import cocoa.plaf.LookAndFeel;
import cocoa.tableView.TableView;
import cocoa.tableView.TextLineLinkedListEntry;
import cocoa.tableView.TextTableColumn;
import cocoa.text.TextLineRendererFactory;
import cocoa.util.StringUtil;

import flash.display.DisplayObject;
import flash.errors.IllegalOperationError;
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
  private static var smallRightSidePopUpButtonArrowsClass:Class;

  public function ValueTableColumn(laf:LookAndFeel, rendererFactory:TextLineRendererFactory, tableView:TableView, textInsets:Insets) {
    super(null, rendererFactory, tableView, textInsets);

    dataSource = MyTableViewDataSource(tableView.dataSource);
    if (smallRightSidePopUpButtonArrowsClass == null) {
      smallRightSidePopUpButtonArrowsClass = laf.getClass("small.rightSidePopUpButtonArrows");
    }
  }

  override public function createAndLayoutRenderer(rowIndex:int, x:Number, y:Number):DisplayObject {
    var description:Object = dataSource.getObjectValue(this, rowIndex);
    var type:String = description.type;
    var object:Object = dataSource.object;
    var text:String;
    var customElementFormat:ElementFormat;
    var newEntry:TextLineLinkedListEntry;
    var createTextLine:Boolean = true;
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
          newEntry = TextLineLinkedListEntry.create(null);
          createTextLine = false;
        }

        var annotations:Array = description.metadata;
        var enumeration:String;
        var defaultValue:String;
        if (annotations != null && annotations.length != 0) {
          for each (var annotation:Object in annotations) {
            if (annotation.name == "Inspectable") {
              for each (var item:Object in annotation.value) {
                switch (item.key) {
                  case "enumeration":
                    enumeration = item.value;
                    break;
                  case "defaultValue":
                    defaultValue = item.value;
                    break;
                }
              }
              break;
            }
          }
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
        else if (v == null) {
          text = "null";
          customElementFormat = func;
        }
        else {
          // use only overriden toString function
          if (v.hasOwnProperty("toString")) {
            text = v.toString();
          }
          else {
            text = StringUtil.startsWith(type, "__AS3__.vec::Vector.<") ? "<>[…]" : "{…}";
          }
        }
    }

    var line:TextLine;
    if (createTextLine) {
      line = textLineRendererFactory.create(text, actualWidth, customElementFormat);
      line.x = x + textInsets.left;
      line.y = y + tableView.rowHeight - textInsets.bottom;
    }

    if (newEntry == null) {
      if (enumeration == null) {
        newEntry = TextLineLinkedListEntry.create(line);
      }
      else {
        var e:TextLineAndDisplayObjectLinkedListEntry = TextLineAndDisplayObjectLinkedListEntry.create(line, smallRightSidePopUpButtonArrowsClass);
        newEntry = e;
        var displayObject:DisplayObject = e.displayObject;
        if (displayObject.parent != textLineRendererFactory.container) {
          textLineRendererFactory.container.addChild(displayObject);
        }
        displayObject.y = y + 2;
        displayObject.x = x + actualWidth - displayObject.width - 4;
      }
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

  override public function postLayout():void {
    super.postLayout();

    clearOurPools();
  }

  private function clearOurPools():void {
    var fuckedClass:Class = TextLineAndDisplayObjectLinkedListEntry;
    for (var i:int = TextLineAndDisplayObjectLinkedListEntry.oldPoolSize1, n:int = TextLineAndDisplayObjectLinkedListEntry.poolSize1; i < n; i++) {
      textLineRendererFactory.container.removeChild(TextLineAndDisplayObjectLinkedListEntry.pool1[i].displayObject);
    }

    for (var j:int = 0; j < TextLineAndDisplayObjectLinkedListEntry.poolSize1; j++) {
      if (TextLineAndDisplayObjectLinkedListEntry.pool1[j].displayObject.parent != null) {
        throw new IllegalOperationError();
      }
    }
  }

  override public function reuse(rowCountDelta:int, finalPass:Boolean):void {
    TextLineAndDisplayObjectLinkedListEntry.oldPoolSize1 = TextLineAndDisplayObjectLinkedListEntry.poolSize1;
    super.reuse(rowCountDelta, finalPass);

    if (finalPass) {
      clearOurPools();
    }
  }
}
}

import cocoa.tableView.TextLineLinkedListEntry;

import flash.display.DisplayObject;
import flash.text.engine.TextLine;

class TextLineAndDisplayObjectLinkedListEntry extends TextLineLinkedListEntry {
  internal static const pool1:Vector.<TextLineAndDisplayObjectLinkedListEntry> = new Vector.<TextLineAndDisplayObjectLinkedListEntry>(32, true);
  internal static var poolSize1:int;

  internal static var oldPoolSize1:int;

  public var displayObject:DisplayObject;

  function TextLineAndDisplayObjectLinkedListEntry(line:TextLine, displayObject:DisplayObject) {
    super(line);
    this.displayObject = displayObject;
  }

  public static function create(line:TextLine, displayObjectClass:Class):TextLineAndDisplayObjectLinkedListEntry {
    if (poolSize1 == 0) {
      return new TextLineAndDisplayObjectLinkedListEntry(line, new displayObjectClass());
    }
    else {
      var entry:TextLineAndDisplayObjectLinkedListEntry = pool1[--poolSize1];
      entry.line = line;
      return entry;
    }
  }

  override public function addToPool():void {
    if (poolSize1 == pool1.length) {
      pool1.fixed = false;
      pool1.length = poolSize1 << 1;
      pool1.fixed = true;
    }
    pool1[poolSize1++] = this;
  }

}