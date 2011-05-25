package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.Insets;
import cocoa.plaf.LookAndFeelUtil;
import cocoa.plaf.TextFormatId;
import cocoa.tableView.TableColumn;
import cocoa.tableView.TableView;
import cocoa.tableView.TextTableColumn;
import cocoa.text.TextLineRendererFactory;

import com.intellij.flex.uiDesigner.ui.inspectors.AbstractTitledBlockItemRenderer;

import flash.display.DisplayObject;

public class PropertyList extends AbstractTitledBlockItemRenderer {
  private const tableView:TableView = new TableView();
  private var source:MyTableViewDataSource;

  //private function createGridColumn(headerText:String):GridColumn {
  //  var column:GridColumn = new GridColumn();
  //  column.headerText = headerText;
  //  //column.dataField = dataField;
  //  return column;
  //}

  private var dataChanged:Boolean;
  private var _object:Object;
  public function get data():Object {
    return _object;
  }

  public function set data(value:Object):void {
    if (value == null || value == _object) {
      return;
    }
    
    _object = value;
    dataChanged = true;
    invalidateProperties();
  }

  override protected function createChildren():void {
    super.createChildren();

    laf = LookAndFeelUtil.find(parent);
    labelHelper.text = "Other";

    tableView.dataSource = source = new MyTableViewDataSource();
    tableView.minNumberOfRows = 3;
    var insets:Insets = new Insets(2, NaN, NaN, 3);
    var textLineRendererFactory:TextLineRendererFactory = new TextLineRendererFactory(laf.getTextFormat(TextFormatId.SMALL_SYSTEM));
    var firstColumn:TextTableColumn = new TextTableColumn("name", textLineRendererFactory, tableView, insets);
    firstColumn.width = 120;
    tableView.columns = new <TableColumn>[firstColumn];

    var skin:DisplayObject = DisplayObject(tableView.createView(laf));
    skin.y = border.layoutHeight;
    addChild(skin);
  }

  override protected function measure():void {
    measuredHeight = border.layoutHeight + tableView.skin.getExplicitOrMeasuredHeight();
  }

  override protected function commitProperties():void {
    super.commitProperties();

    if (dataChanged) {
      dataChanged = false;
      source.update(_object);
    }
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    if (w == 0) {
      return;
    }

    super.updateDisplayList(w, h);

    tableView.skin.setActualSize(w, h - border.layoutHeight);
  }
}
}

import avmplus.HIDE_OBJECT;
import avmplus.INCLUDE_ACCESSORS;
import avmplus.INCLUDE_METADATA;
import avmplus.INCLUDE_TRAITS;
import avmplus.INCLUDE_VARIABLES;
import avmplus.describe;

import cocoa.tableView.TableColumn;
import cocoa.tableView.TableViewDataSource;

import flash.errors.IllegalOperationError;

class MyTableViewDataSource implements TableViewDataSource {
  private const source:Vector.<Object> = new Vector.<Object>(64);
  private var sourceItemCounter:int = 0;

  public function update(object:Object):void {
    var traits:Object = describe(object, INCLUDE_ACCESSORS | INCLUDE_VARIABLES | INCLUDE_METADATA | HIDE_OBJECT | INCLUDE_TRAITS).traits;
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

    source[sourceItemCounter++] = accessor;
  }

  public function get numberOfRows():int {
    return sourceItemCounter;
  }

  public function getValue(column:TableColumn, rowIndex:int):Object {
    throw new IllegalOperationError();
  }

  public function getStringValue(column:TableColumn, rowIndex:int):String {
    return source[rowIndex][column.dataField];
  }
}
