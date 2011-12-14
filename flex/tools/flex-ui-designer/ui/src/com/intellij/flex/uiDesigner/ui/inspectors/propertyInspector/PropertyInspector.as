package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.ListView;
import cocoa.Panel;
import cocoa.View;
import cocoa.layout.ListVerticalLayout;
import cocoa.pane.PaneItem;
import cocoa.pane.PaneViewDataSource;
import cocoa.renderer.PaneRendererManager;

import com.intellij.flex.uiDesigner.ui.CustomTextFormatId;

public class PropertyInspector extends Panel {
  private var dataSource:PaneViewDataSource = new PaneViewDataSource((source = new Vector.<PaneItem>()));
  private var source:Vector.<PaneItem>;
 
  private var otherPropertiesPane:PaneItem;
 
  public function set element(value:Object):void {
    updateData(value);
  }

  override public function get contentView():View {
    var contentView:View = super.contentView;
    if (contentView != null) {
      return contentView;
    }

    var listView:ListView = new ListView();
    listView.dataSource = dataSource;
    //noinspection UnnecessaryLocalVariableJS
    var layout:ListVerticalLayout = new ListVerticalLayout();
    listView.layout = layout;
    listView.rendererManager = new PaneRendererManager(laf.getTextFormat(CustomTextFormatId.SIDE_PANE_GROUP_ITEM_LABEL), laf.getBorder("GroupItemRenderer.b"), laf);

    //var scrollView:ScrollView = new ScrollView();
    //scrollView.documentView = listView;
    //scrollView.horizontalScrollPolicy = ScrollPolicy.OFF;

    this.contentView = listView;
    return listView;
  }
 
  protected function updateData(element:Object):void {
    if (element == null) {
      clear();
      emptyText = "No Selection";
    }
    else if (!isApplicable(element)) {
      clear();
      emptyText = "Not Applicable";
    }
    else {
      emptyText = null;
      doUpdateData(element);
      dataSource.reset.dispatch();
    }
  }
 
  protected function doUpdateData(element:Object):void {
    if (otherPropertiesPane == null) {
      otherPropertiesPane = new PaneItem(null, null);
      otherPropertiesPane.localizedTitle = "Other";
      otherPropertiesPane.viewFactory = new OtherPropertiesFactory();
    }

    source.fixed = false;
    source.length = 1;
    source[0] = otherPropertiesPane;
    source.fixed = true;

    OtherPropertiesFactory(otherPropertiesPane.viewFactory).dataSource.update(element);
  }
 
  //noinspection JSMethodCanBeStatic
  protected function isApplicable(element:Object):Boolean {
    return true;
  }
 
  private function clear():void {
    if (dataSource != null) {
      source.fixed = false;
      dataSource.clear();
      source.fixed = true;
    }
  }
}
}

import cocoa.Insets;
import cocoa.ScrollPolicy;
import cocoa.View;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.TextFormatId;
import cocoa.renderer.ViewFactory;
import cocoa.tableView.TableColumn;
import cocoa.tableView.TableColumnImpl;
import cocoa.tableView.TableView;
import cocoa.text.TextFormat;

import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.MyTableViewDataSource;
import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.NameRendererManager;
import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.PropertyTableInteractor;
import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.ValueRendererManager;

import flash.display.DisplayObjectContainer;

class OtherPropertiesFactory implements ViewFactory {
  internal var dataSource:MyTableViewDataSource = new MyTableViewDataSource();

  public function newInstance():* {
  }

  public function create(laf:LookAndFeel, container:DisplayObjectContainer):View {
    var tableView:TableView = new TableView();
    tableView.verticalScrollPolicy = ScrollPolicy.OFF;
    tableView.dataSource = dataSource;
    tableView.minRowCount = 3;
    var insets:Insets = new Insets(2, NaN, NaN, 3);
    var textFormat:TextFormat = laf.getTextFormat(TextFormatId.SMALL_SYSTEM);
    var firstColumn:TableColumn = new TableColumnImpl(tableView, "name", new NameRendererManager(textFormat, insets));
    firstColumn.width = 160;
    var valueRendererManager:ValueRendererManager = new ValueRendererManager(laf, textFormat, insets, dataSource);
    tableView.columns = new <TableColumn>[firstColumn, new TableColumnImpl(tableView, null, valueRendererManager)];
    tableView.addToSuperview(container, laf, null);
    new PropertyTableInteractor(tableView, valueRendererManager);
    return tableView;
  }
}