package com.intellij.flex.uiDesigner.ui {
import cocoa.AbstractComponent;
import cocoa.ClassFactory;
import cocoa.pane.PaneItem;
import cocoa.pane.PaneViewDataSource;
import cocoa.resources.ResourceMetadata;
import cocoa.sidebar.Sidebar;
import cocoa.tabView.TabView;
import cocoa.ui;

import com.intellij.flex.uiDesigner.Document;
import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.PropertyInspector;
import com.intellij.flex.uiDesigner.ui.inspectors.styleInspector.StyleInspector;

import flash.utils.Dictionary;

import org.flyti.plexus.Injectable;

use namespace ui;

[ResourceBundle("Designer")]
public class ProjectView extends AbstractComponent implements Injectable {
  private static const _skinParts:Dictionary = new Dictionary();
  _skinParts.editorTabView = 0;
  _skinParts.sidebar = 0;

  ui var sidebar:Sidebar;
  ui var editorTabView:TabView;

  private const editorTabsSource:Vector.<PaneItem> = new Vector.<PaneItem>();
  private const editorPanes:PaneViewDataSource = new PaneViewDataSource(editorTabsSource);
  
  private var _documentFactoryManager:DocumentFactoryManager;
  public function set documentFactoryManager(value:DocumentFactoryManager):void {
    _documentFactoryManager = value;
  }

  override protected function get skinParts():Dictionary {
    return _skinParts;
  }

  ui function sidebarAdded():void {
    // todo move
    sidebar.dataSource = new PaneViewDataSource(new <PaneItem>[
      new PaneItem(new ResourceMetadata("style", "Designer"), new ClassFactory(StyleInspector)),
      new PaneItem(new ResourceMetadata("properties", "Designer"), new ClassFactory(PropertyInspector))
    ]);
    sidebar.selectedIndices = new <int>[0, 1];
  }

  ui function editorTabViewAdded():void {
    editorTabView.dataSource = editorPanes;
    editorTabView.selectionChanged.add(editorTabSelectionChanged);
    
    editorPanes.itemRemoved.add(editorPaneRemoved);
  }

  private static function editorTabSelectionChanged(oldItem:DocumentPaneItem, newItem:DocumentPaneItem):void {
    if (oldItem != null) {
      oldItem.document.systemManager.deactivated();
    }
    if (newItem != null) {
      newItem.document.systemManager.activated();
    }
  }

  override protected function get primaryLaFKey():String {
    return "ProjectView";
  }

  public function addDocument(document:Document):void {
    var paneItem:DocumentPaneItem = new DocumentPaneItem(document);
    paneItem.localizedTitle = document.file.name.substring(0, document.file.name.lastIndexOf("."));

    editorPanes.addItem(paneItem);
  }

  public function selectEditorTab(document:Document):void {
    var index:int = -1;
    for (var i:int = 0; i < editorTabsSource.length; i++) {
      if (DocumentPaneItem(editorTabsSource[i]).document == document) {
        index = i;
        break;
      }
    }

    if (index != -1) {
      editorTabView.selectedIndex = index;
    }
  }

  //noinspection JSUnusedLocalSymbols
  private function editorPaneRemoved(item:DocumentPaneItem, index:int):void {
    _documentFactoryManager.unregister(item.document);
  }
}
}

import cocoa.pane.PaneItem;

import com.intellij.flex.uiDesigner.Document;

final class DocumentPaneItem extends PaneItem {
  private var _document:Document;
  public function get document():Document {
    return _document;
  }
  
  public function DocumentPaneItem(document:Document) {
    _document = document;
    view = document.container;
    
    super(null, null);
  }
}
