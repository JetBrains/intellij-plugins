package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.ListView;
import cocoa.Panel;
import cocoa.layout.ListVerticalLayout;
import cocoa.pane.PaneItem;
import cocoa.pane.PaneViewDataSource;
import cocoa.renderer.EntryFactory;
import cocoa.renderer.PaneRendererManager;
import cocoa.renderer.PaneViewBuilderItem;

import com.intellij.flex.uiDesigner.ui.CustomTextFormatId;

public class PropertyInspector extends Panel {
  private var dataSource:PaneViewDataSource;
  private var source:Vector.<PaneItem>;
 
  private var otherPropertiesPane:PaneViewBuilderItem = new PaneViewBuilderItem(new OtherPropertiesFactory(), "Other");
 
  public function set element(value:Object):void {
    updateData(value);
  }
 
  override protected function skinAttached():void {
    dataSource = new PaneViewDataSource((source = new Vector.<PaneItem>()));
 
    var listView:ListView = new ListView();
    listView.dataSource = dataSource;
    var layout:ListVerticalLayout = new ListVerticalLayout();
    listView.layout = layout;
    listView.rendererManager = new PaneRendererManager(laf,
                                                       laf.getTextFormat(CustomTextFormatId.SIDE_PANE_GROUP_ITEM_LABEL),
                                                       laf.getBorder("GroupItemRenderer.b"),
                                                       new <EntryFactory>[otherPropertiesPane.builder]);
 
    //var scrollView:ScrollView = new ScrollView();
    //scrollView.documentView = listView;
    //scrollView.horizontalScrollPolicy = ScrollPolicy.OFF;
 
    contentView = listView;
 
    super.skinAttached();
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
    }
 
    doUpdateData(element);
    dataSource.reset.dispatch();
  }
 
  protected function doUpdateData(element:Object):void {
    var otherPropertiesFactory:OtherPropertiesFactory = OtherPropertiesFactory(otherPropertiesPane.builder);

    source.fixed = false;
    source.length = 1;
    source[0] = otherPropertiesPane;
    source.fixed = true;

    otherPropertiesFactory.dataSource.update(element);
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
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.TextFormatId;
import cocoa.renderer.CompositeEntry;
import cocoa.renderer.CompositeEntryBuilder;
import cocoa.tableView.TableColumn;
import cocoa.tableView.TableColumnImpl;
import cocoa.tableView.TableView;
import cocoa.text.TextFormat;

import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.MyTableViewDataSource;
import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.NameRendererManager;
import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.PropertyTableInteractor;
import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.ValueRendererManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;

class OtherPropertiesFactory extends CompositeEntryBuilder {
  internal var dataSource:MyTableViewDataSource = new MyTableViewDataSource();

  public function OtherPropertiesFactory() {
    super(1);
  }

  override protected function doBuild(e:CompositeEntry, laf:LookAndFeel, container:DisplayObjectContainer):void {
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
    tableView.addToSuperview(container);
    new PropertyTableInteractor(tableView, valueRendererManager);

    e.components[0] = tableView;
  }
}