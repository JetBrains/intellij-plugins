package com.intellij.flex.uiDesigner.plaf {
import cocoa.Border;
import cocoa.Insets;
import cocoa.ItemMouseSelectionMode;
import cocoa.PushButton;
import cocoa.SkinnableView;
import cocoa.View;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.TextFormatId;
import cocoa.renderer.InteractiveGraphicsRendererManager;
import cocoa.renderer.LayeringMode;
import cocoa.renderer.TextLineAndDisplayObjectEntry;
import cocoa.renderer.TextLineAndDisplayObjectEntryFactory;
import cocoa.renderer.ViewEntry;
import cocoa.renderer.ViewEntryFactory;

import flash.display.DisplayObject;
import flash.display.Graphics;
import flash.display.Shape;
import flash.text.engine.TextLine;

public class EditorTabBarRendererManager extends InteractiveGraphicsRendererManager {
  protected var myFactory:ViewEntryFactory = new ViewEntryFactory();
  private var laf:LookAndFeel;

  //noinspection JSUnusedLocalSymbols
  public function EditorTabBarRendererManager(laf:LookAndFeel, lafKey:String) {
    super(laf.getTextFormat(TextFormatId.SYSTEM), new Insets(7, 0, 0, 10));
    this.laf = laf;
  }

  override protected function createFactory():TextLineAndDisplayObjectEntryFactory {
    return myFactory;
  }

  override protected function get layeringMode():int {
    return LayeringMode.DESCENDING_ORDER;
  }

  override public function get mouseSelectionMode():int {
    return ItemMouseSelectionMode.DOWN;
  }

  override protected function computeCreatingRendererSize(w:int, h:int, line:TextLine):void {
    _lastCreatedRendererDimension = Math.round(line.textWidth) + (1 + 6 + 1 + 2 + 16 + 2);
  }

  override protected function doCreateEntry(line:TextLine, itemIndex:int):TextLineAndDisplayObjectEntry {
    var entry:ViewEntry = ViewEntry(myFactory.create(line));
    var closeButton:PushButton = PushButton(entry.view);
    if (closeButton == null) {
      closeButton = new PushButton();
      entry.view = closeButton;
      closeButton.setAction(closeTab, entry);
      closeButton.lafSubkey = "TabLabel";
    }

    return entry;
  }

  override protected function addToDisplayList(entry:TextLineAndDisplayObjectEntry, displayIndex:int):void {
    super.addToDisplayList(entry, displayIndex);

    var closeButton:SkinnableView = SkinnableView(ViewEntry(entry).view);
    var skin:View = closeButton.skin;
    if (skin == null) {
      closeButton.addToSuperview(_textLineContainer, laf, null);
      closeButton.setSize(closeButton.getPreferredWidth(), closeButton.getPreferredHeight());
    }
    else {
      var displayObject:DisplayObject = DisplayObject(skin);
      if (displayObject.parent == null) {
        _textLineContainer.addChild(displayObject);
      }
    }

    closeButton.validate();
  }

  private function closeTab(entry:TextLineAndDisplayObjectEntry):void {
    EditorTabViewSkin(_container.parent).closeTab(entry.itemIndex);
  }

  override public function setSelected(itemIndex:int, relatedIndex:int, value:Boolean):void {
    var entry:TextLineAndDisplayObjectEntry = findEntry2(itemIndex);
    var g:Graphics = Shape(entry.displayObject).graphics;
    g.clear();
    drawBackground(g, entry.line.userData, entry.displayObject.x, value);
  }

  override public function removeRenderer(itemIndex:int, x:Number, y:Number, w:Number, h:Number):void {
    super.removeRenderer(itemIndex, x, y, w, h);

    var selectedIndex:int = EditorTabViewSkin(_container.parent).getSelectedIndex();
    if (selectedIndex != -1) {
      setSelected(selectedIndex, -1, true);
    }
  }

  override protected function drawEntry(entry:TextLineAndDisplayObjectEntry, itemIndex:int, g:Graphics, w:int, h:int, x:Number, y:Number):void {
    var view:View = ViewEntry(entry).view;
    view.setLocation((x + w) - 2 - 1 - view.getPreferredWidth(), view.y);
    //skin.x = (x + w) - 2 - 1 - skin.getExplicitOrMeasuredWidth();
    drawBackground(g, w, x, _selectionModel.isItemSelected(itemIndex));
  }

  private function drawBackground(g:Graphics, w:Number, x:Number, selected:Boolean):void {
    var border:Border = laf.getBorder(selected ? "EditorTab.selected" : "EditorTab.unselected");
    border.draw(g, w, _fixedRendererDimension);
  }

  public function clearSelected():void {
    _textLineContainer.graphics.clear();
  }
}
}
