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

import org.osflash.signals.ISignal;
import org.osflash.signals.Signal;

public class MyTableViewDataSource implements TableViewDataSource {
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

    var traits:Object = describe(object, INCLUDE_ACCESSORS | INCLUDE_VARIABLES | INCLUDE_METADATA | HIDE_OBJECT | INCLUDE_TRAITS).traits;
    for each (var accessor:Object in traits.accessors) {
      processProperty(accessor);
    }
    for each (var variable:Object in traits.variables) {
      processProperty(variable);
    }

    source.length = sourceItemCounter;
    if (_reset != null) {
      _reset.dispatch();
    }
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

    if (name == "focusManager" || name == "resourceManager" || name == "cursorManager" || name == "systemManager" || name == "styleManager" ||
      name == "parent" || name == "parentDocument" || name == "screen" || name == "moduleFactory" ||
      name == "inheritingStyles" || name == "nonInheritingStyles" || name == "styleDeclaration" || name == "styleName" ||
      name == "focusPane" || name == "currentState" ||
      name == "dropTarget" || name == "graphics") {
      return;
    }

    if (sourceItemCounter > source.length) {
      source.length = sourceItemCounter + 8;
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
