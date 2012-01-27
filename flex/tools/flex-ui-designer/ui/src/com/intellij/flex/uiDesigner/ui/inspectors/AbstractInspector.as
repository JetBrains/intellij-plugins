package com.intellij.flex.uiDesigner.ui.inspectors {
import cocoa.ListView;
import cocoa.ListViewDataSource;
import cocoa.Panel;
import cocoa.ScrollPolicy;
import cocoa.ScrollView;
import cocoa.View;
import cocoa.layout.ListVerticalLayout;
import cocoa.renderer.RendererManager;

import flash.errors.IllegalOperationError;

[Abstract]
public class AbstractInspector extends Panel {
  protected var dataSource:ListViewDataSource;

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
    listView.rendererManager = createRendererManager(listView);

    var scrollView:ScrollView = new ScrollView();
    scrollView.documentView = listView;
    scrollView.horizontalScrollPolicy = ScrollPolicy.OFF;

    this.contentView = scrollView;
    return scrollView;
  }

  protected function createRendererManager(listView:ListView):RendererManager {
    throw new IllegalOperationError("abstract");
  }

  public function set component(value:Object):void {
    updateData(value);
  }

  protected function isApplicable(element:Object):Boolean {
    return true;
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
    throw new IllegalOperationError("abstract");
  }

  protected function clear():void {
    throw new IllegalOperationError("abstract");
  }
}
}