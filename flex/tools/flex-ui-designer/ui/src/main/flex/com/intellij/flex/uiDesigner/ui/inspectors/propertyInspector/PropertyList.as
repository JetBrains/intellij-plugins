package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.Insets;
import cocoa.plaf.LookAndFeelUtil;
import cocoa.plaf.TextFormatId;
import cocoa.tableView.TableColumn;
import cocoa.tableView.TableColumnImpl;
import cocoa.tableView.TableView;
import cocoa.text.TextFormat;

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
  private var _element:Object;
  public function get data():Object {
    return _element;
  }

  public function set data(value:Object):void {
    if (value == _element) {
      return;
    }
    
    _element = value;
    dataChanged = true;
    invalidateProperties();
  }

  override protected function createChildren():void {
    super.createChildren();

    laf = LookAndFeelUtil.find(parent);
    labelHelper.text = "Other";

    var dataSource:MyTableViewDataSource = new MyTableViewDataSource();
    tableView.dataSource = source = dataSource;
    tableView.minRowCount = 3;
    var insets:Insets = new Insets(2, NaN, NaN, 3);
    var textFormat:TextFormat = laf.getTextFormat(TextFormatId.SMALL_SYSTEM);
    var firstColumn:TableColumn = new TableColumnImpl(tableView, "name", new NameRendererManager(textFormat, insets));
    firstColumn.width = 160;
    var valueRendererManager:ValueRendererManager = new ValueRendererManager(laf, textFormat, insets, dataSource);
    tableView.columns = new <TableColumn>[firstColumn, new TableColumnImpl(tableView, null, valueRendererManager)];

    var skin:DisplayObject = DisplayObject(tableView.createView(laf));
    skin.y = border.layoutHeight;
    addChild(skin);

    new Interactor(tableView, valueRendererManager);
  }

  override protected function measure():void {
    measuredHeight = border.layoutHeight + tableView.skin.getExplicitOrMeasuredHeight();
  }

  override protected function commitProperties():void {
    super.commitProperties();

    if (dataChanged) {
      dataChanged = false;
      source.update(_element);
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