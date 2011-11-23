package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.ClassFactory;
import cocoa.ListView;
import cocoa.Panel;
import cocoa.ScrollPolicy;
import cocoa.ScrollView;
import cocoa.layout.ListVerticalLayout;
import cocoa.pane.PaneItem;
import cocoa.pane.PaneViewDataSource;
import cocoa.renderer.PaneRendererManager;

import com.intellij.flex.uiDesigner.ui.CustomTextFormatId;

public class PropertyInspector extends Panel {
  private var dataSource:PaneViewDataSource;
  private var source:Vector.<PaneItem>;

  private var otherPropertiesPane:PaneItem = PaneItem.create(new ClassFactory(PropertyList), "Other");

  public function set element(value:Object):void {
    updateData(value);
  }

  override protected function skinAttached():void {
    dataSource = new PaneViewDataSource((source = new Vector.<PaneItem>()));

    var listView:ListView = new ListView();
    listView.dataSource = dataSource;
    listView.layout = new ListVerticalLayout();
    listView.rendererManager = new PaneRendererManager(laf.getTextFormat(CustomTextFormatId.SIDE_PANE_GROUP_ITEM_LABEL), laf.getBorder("GroupItemRenderer.b"));

    var scrollView:ScrollView = new ScrollView();
    scrollView.documentView = listView;
    scrollView.horizontalScrollPolicy = ScrollPolicy.OFF;

    contentView = scrollView;

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
  }

  protected function doUpdateData(element:Object):void {
    source.fixed = false;
    source[0] = otherPropertiesPane;
    source.fixed = true;
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