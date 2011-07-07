package com.intellij.flex.uiDesigner.ui {
import cocoa.AbstractCollectionViewDataSource;
import cocoa.Insets;
import cocoa.ListViewDataSource;
import cocoa.SegmentedControl;
import cocoa.plaf.LookAndFeelUtil;
import cocoa.plaf.TextFormatId;
import cocoa.renderer.InteractiveTextRendererManager;

import com.intellij.flex.uiDesigner.ElementManager;
import com.intellij.flex.uiDesigner.flex.SystemManagerSB;

import flash.errors.IllegalOperationError;
import flash.utils.getQualifiedClassName;

import org.flyti.plexus.Injectable;

public class ElementTreeBarManager extends AbstractCollectionViewDataSource implements Injectable, ListViewDataSource {
  private const source:Vector.<String> = new Vector.<String>(8);

  private var elementManager:ElementManager;

  public function ElementTreeBarManager(elementManager:ElementManager) {
    this.elementManager = elementManager;
  }

  public function set element(value:Object):void {
    if (_presentation == null) {
      return;
    }
    
    if (value == null) {
      //_presentation.hidden = true;
      return;
    }

    update(value);

    //_presentation.hidden = false;
  }

  private var _presentation:SegmentedControl;
  public function set presentation(value:SegmentedControl):void {
    if (_presentation == value) {
      return;
    }

    _presentation = value;
    _presentation.dataSource = this;

    var insets:Insets = new Insets(2, NaN, NaN, 3);
    _presentation.rendererManager = new InteractiveTextRendererManager(LookAndFeelUtil.find(_presentation).getTextFormat(TextFormatId.SMALL_SYSTEM), insets);
  }

  public function update(object:Object):void {
    _itemCount = 0;

    if (object == null) {
      if (_reset != null) {
        _reset.dispatch();
      }
      return;
    }

    var element:Object = object;
    do {
      if (elementManager.isSkin(element)) {
        continue;
      }

      var qualifiedClassName:String = getQualifiedClassName(element);
      source[_itemCount++] = qualifiedClassName.substr(qualifiedClassName.lastIndexOf("::") + 2);
    }
    while (!((element = element.parent) is SystemManagerSB));

    source.length = _itemCount;
    if (_reset != null) {
      _reset.dispatch();
    }
  }

  public function getObjectValue(itemIndex:int):Object {
    throw new IllegalOperationError();
  }

  public function getStringValue(itemIndex:int):String {
    return source[_itemCount - 1 - itemIndex];
  }
}
}