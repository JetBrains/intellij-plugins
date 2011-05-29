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
    tableView.minRowCount = 3;
    tableView.lafSubkey = "small";
    var insets:Insets = new Insets(2, NaN, NaN, 3);
    var textLineRendererFactory:TextLineRendererFactory = new TextLineRendererFactory(laf.getTextFormat(TextFormatId.SMALL_SYSTEM));
    var firstColumn:TextTableColumn = new TextTableColumn("name", textLineRendererFactory, tableView, insets);
    firstColumn.width = 140;
    tableView.columns = new <TableColumn>[firstColumn, new ValueTableColumn(textLineRendererFactory, tableView, insets)];

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

