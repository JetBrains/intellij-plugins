package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import avmplus.HIDE_OBJECT;
import avmplus.INCLUDE_ACCESSORS;
import avmplus.INCLUDE_METADATA;
import avmplus.INCLUDE_TRAITS;
import avmplus.INCLUDE_VARIABLES;
import avmplus.describe;

import cocoa.plaf.LookAndFeel;

import com.intellij.flex.uiDesigner.ui.inspectors.AbstractTitledBlockItemRenderer;

import mx.core.IDataRenderer;

import org.flyti.util.ArrayList;


public class PropertyList extends AbstractTitledBlockItemRenderer implements IDataRenderer {
  //private const dataGrid:DataGrid = new DataGrid();

  public function PropertyList() {
    //dataGrid.columns = new ArrayList(new <Object>[createGridColumn("Property"), createGridColumn("Value")]);
  }

  //private function createGridColumn(headerText:String):GridColumn {
  //  var column:GridColumn = new GridColumn();
  //  column.headerText = headerText;
  //  //column.dataField = dataField;
  //  return column;
  //}

  private const source:Vector.<Object> = new Vector.<Object>(64);
  private var sourceItemCounter:int = 0;
  private const sourceList:ArrayList = new ArrayList(source);

  override public function set laf(value:LookAndFeel):void {
    super.laf = value;
    labelHelper.text = "Other";
    //dataGrid.y = border.layoutHeight;
  }

  override protected function createChildren():void {
    //addChild(dataGrid);
  }

  private var _object:Object;
  public function get data():Object {
    return _object;
  }

  public function set data(value:Object):void {
    if (value == null || value == _object) {
      return;
    }
    
    _object = value;

    var traits:Object = describe(value, INCLUDE_ACCESSORS | INCLUDE_VARIABLES | INCLUDE_METADATA | HIDE_OBJECT | INCLUDE_TRAITS).traits;
    for each (var accessor:Object in traits.accessors) {
      processProperty(accessor);
    }
    for each (var variable:Object in traits.variables) {
      processProperty(variable);
    }

    //if (dataGrid.dataProvider == null) {
    //  dataGrid.dataProvider = sourceList;
    //}
    //else {
    //  sourceList.refresh();
    //}

    source.length = sourceItemCounter;
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

    if (sourceItemCounter > source.length) {
      source.length = sourceItemCounter + 8;
    }

    source[sourceItemCounter++] = name;
  }

  override protected function measure():void {
    //measuredHeight = border.layoutHeight + dataGrid.getExplicitOrMeasuredHeight();
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    super.updateDisplayList(w, h);

    //dataGrid.setActualSize(w, h - border.layoutHeight);
  }
}
}
