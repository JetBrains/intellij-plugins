package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.CheckBox;
import cocoa.DocumentWindow;
import cocoa.Focusable;
import cocoa.Insets;
import cocoa.TextInput;
import cocoa.plaf.ButtonSkinInteraction;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.Skin;
import cocoa.renderer.CheckBoxEntry;
import cocoa.renderer.CheckBoxEntryFactory;
import cocoa.renderer.TextLineAndDisplayObjectEntry;
import cocoa.renderer.TextLineAndDisplayObjectEntryFactory;
import cocoa.renderer.TextLineEntry;
import cocoa.renderer.TextRendererManager;
import cocoa.text.EditableTextView;
import cocoa.text.TextFormat;
import cocoa.ui;
import cocoa.util.StringUtil;

import com.intellij.flex.uiDesigner.ui.CssElementFormat;

import flash.display.DisplayObject;
import flash.display.InteractiveObject;
import flash.display.Sprite;
import flash.text.engine.ElementFormat;
import flash.text.engine.TextLine;

use namespace ui;

public class ValueRendererManager extends TextRendererManager {
  private static const DISABLED_COLOR:uint = 0x808080;
  internal static const stringDisabled:ElementFormat = new ElementFormat(CssElementFormat.MONOSPACED_FONT_DESCRIPTION, 11, DISABLED_COLOR);
  private static const identifier:ElementFormat = new ElementFormat(CssElementFormat.MONOSPACED_FONT_DESCRIPTION, 11, 0x000080);
  private static const func:ElementFormat = identifier;
  private static const numberFormat:ElementFormat = new ElementFormat(CssElementFormat.MONOSPACED_FONT_DESCRIPTION, 11, 0x0000ff);
  private static const numberFormatDisabled:ElementFormat = stringDisabled;
  private static const staticField:ElementFormat = new ElementFormat(CssElementFormat.MONOSPACED_FONT_DESCRIPTION_ITALIC, 11, 0x660e7a);
  private static const staticFieldDisabled:ElementFormat = new ElementFormat(CssElementFormat.MONOSPACED_FONT_DESCRIPTION_ITALIC, 11, DISABLED_COLOR);

  private var laf:LookAndFeel;
  private var myDataSource:MyTableViewDataSource;

  private static var arrowsFactory:TextLineAndDisplayObjectEntryFactory;
  private static var disabledArrowsFactory:TextLineAndDisplayObjectEntryFactory;
  private static var checkBoxFactory:CheckBoxEntryFactory;

  public function ValueRendererManager(laf:LookAndFeel, textFormat:TextFormat, textInsets:Insets, dataSource:MyTableViewDataSource) {
    this.laf = laf;
    myDataSource = dataSource;

    super(textFormat, textInsets);

    if (arrowsFactory == null) {
      arrowsFactory = new TextLineAndDisplayObjectEntryFactory(laf.getClass("small.arrows"));
      disabledArrowsFactory = new TextLineAndDisplayObjectEntryFactory(laf.getClass("small.arrows.disabled"));
      checkBoxFactory = new CheckBoxEntryFactory();
    }

    registerEntryFactory(arrowsFactory);
    registerEntryFactory(disabledArrowsFactory);
    registerEntryFactory(checkBoxFactory);
  }

  public function getDescription(itemIndex:int):Object {
    return _dataSource.getObjectValue(itemIndex);
  }

  override protected function createEntry(itemIndex:int, x:Number, y:Number, w:Number, h:Number):TextLineEntry {
    var description:Object = getDescription(itemIndex);
    var type:String = description.type;
    var object:Object = myDataSource.object;
    var text:String;
    var customElementFormat:ElementFormat;
    var newEntry:TextLineEntry;
    var createTextLine:Boolean = true;

    var enumeration:String = description.enumeration;
    var editable:Boolean = description.editable;
    var v:*;
    try {
      v = object[description.name];
    }
    catch (e:Error) {
      text = e.message;
      customElementFormat = editable ? numberFormat : numberFormatDisabled;
    }

    if (text == null) {
      switch (type) {
        case "int":
        case "uint":
          text = v;
          customElementFormat = editable ? numberFormat : numberFormatDisabled;
          break;

        case "String":
          text = v;
          if (text == null) {
            text = "null";
            customElementFormat = func;
          }
          else if (text.length == 0) {
            newEntry = TextLineEntry.create(null);
            createTextLine = false;
          }
          break;

        case "Number":
          var number:Number = Number(v);
          if (number != number) {
            text = "NaN";
            customElementFormat = editable ? staticField : staticFieldDisabled;
          }
          else {
            text = number.toString();
            customElementFormat = numberFormat;
          }
          break;

        case "Array":
          text = "[…]";
          break;

        case "Boolean":
          newEntry = createEntryForBoolean(v, editable, x, y);
          createTextLine = false;
          break;

        default:
          if (type == "*" && v === undefined) {
            text = "undefined";
            customElementFormat = editable ? staticField : staticFieldDisabled;
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
    }

    var line:TextLine;
    if (createTextLine) {
      if (customElementFormat == null) {
        customElementFormat = editable ? textFormat.format : stringDisabled;
      }

      line = textLineRendererFactory.create(textLineContainer, text, w, customElementFormat);
      layoutTextLine(line, x, y, h);
    }

    if (newEntry == null) {
      newEntry = enumeration == null ? TextLineEntry.create(line) : createEntryForEnumeration(line, editable, y, x, w);
    }

    newEntry.itemIndex = itemIndex;
    return newEntry;
  }

  private function createEntryForEnumeration(line:TextLine, editable:Boolean, y:Number, x:Number, w:Number):TextLineEntry {
    var e:TextLineAndDisplayObjectEntry = editable ? arrowsFactory.create(line) : disabledArrowsFactory.create(line);
    var displayObject:DisplayObject = e.displayObject;
    if (displayObject.parent != _container) {
      _container.addChild(displayObject);
    }
    displayObject.y = y + 2;
    displayObject.x = x + w - displayObject.width - 4;
    return e;
  }

  private function createEntryForBoolean(value:Boolean, editable:Boolean, x:Number, y:Number):CheckBoxEntry {
    var e:CheckBoxEntry = checkBoxFactory.create(value);
    var checkbox:CheckBox = e.checkbox;
    var skin:Skin = checkbox.skin;
    if (skin == null) {
      skin = checkbox.createView(laf);
      ButtonSkinInteraction(skin).deletegateInteraction();
      skin.validateNow();
      skin.setActualSize(skin.getExplicitOrMeasuredWidth(), skin.getExplicitOrMeasuredHeight());
    }

    skin.enabled = editable;

    var displayObject:DisplayObject = DisplayObject(skin);
    if (displayObject.parent != _container) {
      _container.addChild(displayObject);
    }
    else {
      skin.invalidateDisplayList();
    }

    displayObject.x = x + 3;
    displayObject.y = y + 1;

    return e;
  }

  public function createEditor(itemIndex:int, entry:TextLineEntry, w:Number, h:Number):Sprite {
    var description:Object = getDescription(itemIndex);
    if (description.type != "String" || !description.editable) {
      return null;
    }

    var textInput:TextInput = new TextInput();
    textInput.text = myDataSource.object[description.name];
    var skin:Skin = textInput.createView(laf);

    var displayObject:DisplayObject = DisplayObject(skin);
    displayObject.x = entry.line.x - textInsets.left;
    displayObject.width = w;

    _container.addChild(displayObject);
    _container.mouseChildren = true;
    skin.validateNow();
    var textInputHeight:Number = skin.getExplicitOrMeasuredHeight();
    skin.setActualSize(skin.getExplicitOrMeasuredWidth(), textInputHeight);

    displayObject.y = ((entry.line.y + textInsets.bottom) - h) - Math.round((textInputHeight - h) / 2);

    var textField:EditableTextView = textInput.textDisplay;
    DocumentWindow(_container.stage.nativeWindow).focusManager.setFocus(Focusable(skin));
    return textField;
  }

  public function closeEditor(editor:InteractiveObject):void {
    _container.removeChild(editor.parent);
    _container.mouseChildren = false;
  }

  public function closeEditorAndCommit(editor:InteractiveObject, value:String, entry:TextLineEntry, w:Number):void {
    closeEditor(editor);

    var line:TextLine = entry.line;
    var lineX:Number = line.x;
    var lineY:Number = line.y;
    textLineRendererFactory.recreate(line, textLineContainer, value, w, textFormat.format);
    line.x = lineX;
    line.y = lineY;
  }
}
}