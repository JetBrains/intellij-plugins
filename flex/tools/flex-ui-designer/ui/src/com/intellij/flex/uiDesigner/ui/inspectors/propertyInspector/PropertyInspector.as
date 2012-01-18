package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.ListView;
import cocoa.pane.PaneItem;
import cocoa.pane.PaneViewDataSource;
import cocoa.renderer.PaneRendererManager;
import cocoa.renderer.RendererManager;

import com.intellij.flex.uiDesigner.ui.CustomTextFormatId;

import com.intellij.flex.uiDesigner.ui.inspectors.AbstractInspector;

public class PropertyInspector extends AbstractInspector {
  protected var source:Vector.<PaneItem>;

  private var otherPropertiesPane:PaneItem;

  public function PropertyInspector() {
    dataSource = new PaneViewDataSource((source = new Vector.<PaneItem>()));
  }

  override protected function createRendererManager(listView:ListView):RendererManager {
    return new PaneRendererManager(laf.getTextFormat(CustomTextFormatId.SIDE_PANE_GROUP_ITEM_LABEL), laf.getBorder("GroupItemRenderer.b"), laf, listView);
  }

  override protected function doUpdateData(element:Object):void {
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

  override protected function clear():void {
    if (dataSource != null) {
      source.fixed = false;
      PaneViewDataSource(dataSource).clear();
      source.fixed = true;
    }
  }
}
}

import cocoa.ContentView;
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

  public function create(laf:LookAndFeel, container:DisplayObjectContainer, superview:ContentView):View {
    var tableView:TableView = new TableView();
    tableView.verticalScrollPolicy = ScrollPolicy.OFF;
    tableView.dataSource = dataSource;
    tableView.minRowCount = 3;
    var insets:Insets = new Insets(2, NaN, NaN, 3);
    var textFormat:TextFormat = laf.getTextFormat(TextFormatId.SMALL_SYSTEM);
    var firstColumn:TableColumn = new TableColumnImpl(tableView, "name", new NameRendererManager(textFormat, insets));
    firstColumn.preferredWidth = 160;
    var valueRendererManager:ValueRendererManager = new ValueRendererManager(laf, textFormat, insets, dataSource);
    tableView.columns = new <TableColumn>[firstColumn, new TableColumnImpl(tableView, null, valueRendererManager)];
    tableView.addToSuperview(container, laf, superview);
    new PropertyTableInteractor(tableView, valueRendererManager);
    return tableView;
  }
}