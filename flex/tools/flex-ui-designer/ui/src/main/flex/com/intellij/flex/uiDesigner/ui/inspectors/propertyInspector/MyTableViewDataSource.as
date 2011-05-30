package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import avmplus.HIDE_OBJECT;
import avmplus.INCLUDE_ACCESSORS;
import avmplus.INCLUDE_METADATA;
import avmplus.INCLUDE_TRAITS;
import avmplus.INCLUDE_VARIABLES;
import avmplus.describe;

import cocoa.tableView.TableColumn;
import cocoa.tableView.TableViewDataSource;

import flash.errors.IllegalOperationError;
import flash.utils.Dictionary;

import org.osflash.signals.ISignal;
import org.osflash.signals.Signal;

public class MyTableViewDataSource implements TableViewDataSource {
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
  excludedProperties["screen"] = true;
  excludedProperties["transform"] = true;

  private const source:Vector.<Object> = new Vector.<Object>(64);
  private var sourceItemCounter:int = 0;

  private var _object:Object;
  public function get object():Object {
    return _object;
  }

  private var _reset:ISignal;
  public function get reset():ISignal {
    if (_reset == null) {
      _reset = new Signal();
    }
    return _reset;
  }

  public function update(object:Object):void {
    _object = object;

    sourceItemCounter = 0;

    var traits:Object = describe(object, INCLUDE_ACCESSORS | INCLUDE_VARIABLES | INCLUDE_METADATA | HIDE_OBJECT | INCLUDE_TRAITS).traits;
    for each (var accessor:Object in traits.accessors) {
      processProperty(accessor);
    }
    for each (var variable:Object in traits.variables) {
      processProperty(variable);
    }

    source.length = sourceItemCounter;
    source.sort(compare);
    if (_reset != null) {
      _reset.dispatch();
    }
  }

  private function compare(a:Object, b:Object):Number {
    return a.name < b.name ? -1 : 1;
  }

  private function processProperty(accessor:Object):void {
    if (accessor.uri != null || accessor.access == "writeonly") {
      return;
    }

    var name:String = accessor.name;
    var firstChar:Number = name.charCodeAt(0);
    if (firstChar == 36 /*$*/ || firstChar == 95 /*_*/) {
      return;
    }

    if (accessor.declaredBy == "mx.core::UIComponent") {
      if (name in excludedProperties) {
        return;
      }
    }
    else if (accessor.declaredBy == "spark.components.supportClasses::SkinnableComponent") {
      if (name == "skin") {
        return;
      }
    }
    else if (name == "dropTarget" || name == "graphics") {
      return;
    }

    if (sourceItemCounter > source.length) {
      source.length = sourceItemCounter + 64;
    }

    source[sourceItemCounter++] = accessor;
  }

  public function get rowCount():int {
    return sourceItemCounter;
  }

  public function getObjectValue(column:TableColumn, rowIndex:int):Object {
    return source[rowIndex];
  }

  public function getStringValue(column:TableColumn, rowIndex:int):String {
    return source[rowIndex][column.dataField];
  }
}
}
