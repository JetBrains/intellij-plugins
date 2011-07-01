package com.intellij.flex.uiDesigner.ui {
import cocoa.CollectionView;
import cocoa.CollectionViewDataSource;
import cocoa.Insets;
import cocoa.plaf.LookAndFeelUtil;
import cocoa.plaf.TextFormatId;
import cocoa.plaf.basic.CollectionHorizontalLayout;
import cocoa.tableView.AbstractCollectionViewDataSource;
import cocoa.tableView.TextRendererManager;

import com.intellij.flex.uiDesigner.ElementManager;
import com.intellij.flex.uiDesigner.flex.SystemManagerSB;

import flash.display.DisplayObjectContainer;
import flash.errors.IllegalOperationError;
import flash.utils.getQualifiedClassName;

import org.flyti.plexus.Injectable;

public class ElementTreeBarManager extends AbstractCollectionViewDataSource implements Injectable, CollectionViewDataSource {
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
      _presentation.hidden = true;
      return;
    }

    update(value);

    _presentation.hidden = false;
  }

  private var _presentation:CollectionView;
  public function set presentation(value:CollectionView):void {
    if (_presentation == value) {
      return;
    }

    _presentation = value;
    _presentation.dataSource = this;

    var insets:Insets = new Insets(2, NaN, NaN, 3);
    CollectionHorizontalLayout(_presentation.layout).rendererManager = new TextRendererManager(LookAndFeelUtil.find(DisplayObjectContainer(_presentation.skin)).getTextFormat(TextFormatId.SMALL_SYSTEM), insets);
  }

  public function update(object:Object):void {
    sourceItemCounter = 0;

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
      source[sourceItemCounter++] = qualifiedClassName.substr(qualifiedClassName.lastIndexOf("::") + 2);
    }
    while (!((element = element.parent) is SystemManagerSB));

    source.length = sourceItemCounter;
    if (_reset != null) {
      _reset.dispatch();
    }
  }

  public function getObjectValue(itemIndex:int):Object {
    throw new IllegalOperationError();
  }

  public function getStringValue(itemIndex:int):String {
    return source[sourceItemCounter - 1 - itemIndex];
  }
}
}