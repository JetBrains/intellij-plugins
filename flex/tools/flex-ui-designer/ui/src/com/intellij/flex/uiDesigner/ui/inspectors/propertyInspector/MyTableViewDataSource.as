package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import avmplus.HIDE_NSURI_METHODS;
import avmplus.HIDE_OBJECT;
import avmplus.INCLUDE_ACCESSORS;
import avmplus.INCLUDE_METADATA;
import avmplus.INCLUDE_TRAITS;
import avmplus.INCLUDE_VARIABLES;
import avmplus.describe;

import cocoa.AbstractCollectionViewDataSource;
import cocoa.tableView.TableColumn;
import cocoa.tableView.TableViewDataSource;

import flash.utils.Dictionary;

internal class MyTableViewDataSource extends AbstractCollectionViewDataSource implements TableViewDataSource {
  private static const excludedProperties:Dictionary = new Dictionary();
  excludedProperties["transitions"] = true;
  excludedProperties["activeEffects"] = true;
  excludedProperties["styleParent"] = true;
  excludedProperties["focusManager"] = true;
  excludedProperties["resourceManager"] = true;
  excludedProperties["cursorManager"] = true;
  excludedProperties["systemManager"] = true;
  excludedProperties["styleManager"] = true;
  excludedProperties["parentDocument"] = true;
  excludedProperties["moduleFactory"] = true;
  excludedProperties["inheritingStyles"] = true;
  excludedProperties["nonInheritingStyles"] = true;
  excludedProperties["styleDeclaration"] = true;
  excludedProperties["styleName"] = true;
  excludedProperties["styleParent"] = true;
  excludedProperties["currentState"] = true;
  excludedProperties["focusPane"] = true;
  excludedProperties.screen = true;
  excludedProperties.transform = true;
  excludedProperties.designLayer = true;

  private const source:Vector.<Object> = new Vector.<Object>(64);

  private var _object:Object;
  public function get object():Object {
    return _object;
  }

  public function update(object:Object):void {
    _object = object;
    _itemCount = 0;

    if (_object == null) {
      if (_reset != null) {
        _reset.dispatch();
      }
      return;
    }

    var traits:Object = describe(object, INCLUDE_ACCESSORS | INCLUDE_VARIABLES | INCLUDE_METADATA | HIDE_OBJECT | INCLUDE_TRAITS | HIDE_NSURI_METHODS).traits;
    for each (var accessor:Object in traits.accessors) {
      processProperty(accessor);
    }
    for each (var variable:Object in traits.variables) {
      processProperty(variable);
    }

    source.length = _itemCount;
    source.sort(compare);
    if (_reset != null) {
      _reset.dispatch();
    }
  }

  private static function compare(a:Object, b:Object):Number {
    return a.name < b.name ? -1 : 1;
  }

  private function processProperty(accessor:Object):void {
    if (accessor.access == "writeonly") {
      return;
    }

    var name:String = accessor.name;
    var firstChar:Number = name.charCodeAt(0);
    if (firstChar == 36 /*$*/ || firstChar == 95 /*_*/) {
      return;
    }

    const declaredBy:String = accessor.declaredBy;
    if (declaredBy == "mx.core::UIComponent") {
      if (name in excludedProperties) {
        return;
      }
    }
    else if (declaredBy == "spark.components.supportClasses::SkinnableComponent") {
      if (name == "skin") {
        return;
      }
    }
    else if (declaredBy == "spark.components::Application") {
      if (name == "applicationDPI") {
        return;
      }
    }
    else if (declaredBy == "flash.display::Sprite" || declaredBy == "flash.display::Shape") {
      if (name == "dropTarget" || name == "graphics") {
        return;
      }
    }

    if (_itemCount > source.length) {
      source.length = _itemCount + 64;
    }

    source[_itemCount++] = accessor;
  }

  public function getObjectValue(column:TableColumn, rowIndex:int):Object {
    return source[rowIndex];
  }

  public function getStringValue(column:TableColumn, rowIndex:int):String {
    return source[rowIndex][column.dataField];
  }
}
}