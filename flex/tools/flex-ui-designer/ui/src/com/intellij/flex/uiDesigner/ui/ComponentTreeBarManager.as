package com.intellij.flex.uiDesigner.ui {
import cocoa.AbstractCollectionViewDataSource;
import cocoa.ListViewDataSource;
import cocoa.SegmentedControl;

import com.intellij.flex.uiDesigner.ComponentManager;

import flash.errors.IllegalOperationError;

import org.flyti.plexus.Injectable;

public class ComponentTreeBarManager extends AbstractCollectionViewDataSource implements Injectable, ListViewDataSource {
  private const source:Vector.<String> = new Vector.<String>(8);
  private var componentManager:ComponentManager;

  public function ComponentTreeBarManager(elementManager:ComponentManager) {
    this.componentManager = elementManager;
  }

  public function set component(value:Object):void {
    if (_presentation == null) {
      return;
    }

    if (value == null) {
      _presentation.visible = false;
      return;
    }

    update(value);

    _presentation.visible = true;
  }

  private var _presentation:SegmentedControl;
  public function set presentation(value:SegmentedControl):void {
    if (_presentation == value) {
      return;
    }

    _presentation = value;
    _presentation.dataSource = this;
  }

  private function update(component:Object):void {
    _itemCount = 0;

    if (component == null) {
      if (_reset != null) {
        _reset.dispatch();
      }
      return;
    }

    _itemCount = componentManager.fillBreadcrumbs(component, source);
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

  public function getItemIndex(object:Object):int {
    return source.indexOf(object);
  }
}
}