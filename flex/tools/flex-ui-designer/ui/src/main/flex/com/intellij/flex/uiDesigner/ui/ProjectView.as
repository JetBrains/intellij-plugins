package com.intellij.flex.uiDesigner.ui {
import cocoa.AbstractComponent;
import cocoa.ClassFactory;
import cocoa.pane.PaneItem;
import cocoa.resources.ResourceMetadata;
import cocoa.sidebar.Sidebar;
import cocoa.tabView.TabView;
import cocoa.ui;

import com.intellij.flex.uiDesigner.Document;
import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.PropertyInspector;
import com.intellij.flex.uiDesigner.ui.inspectors.styleInspector.StyleInspector;

import flash.utils.Dictionary;

import mx.events.CollectionEvent;
import mx.events.CollectionEventKind;

import org.flyti.plexus.Injectable;
import org.flyti.util.ArrayList;
import org.flyti.util.List;

use namespace ui;

[ResourceBundle("Designer")]
public class ProjectView extends AbstractComponent implements Injectable {
  private static const _skinParts:Dictionary = new Dictionary();
  _skinParts.editorTabView = 0;
  _skinParts.sidebar = 0;

  ui var sidebar:Sidebar;
  ui var editorTabView:TabView;

  private const editorPanes:List = new ArrayList();
  
  private var _documentFactoryManager:DocumentFactoryManager;
  public function set documentFactoryManager(value:DocumentFactoryManager):void {
    _documentFactoryManager = value;
  }

  override protected function get skinParts():Dictionary {
    return _skinParts;
  }

  ui function sidebarAdded():void {
    // todo move
    sidebar.items = new ArrayList(new <Object>[
      new PaneItem(new ResourceMetadata("style", "Designer"), new ClassFactory(StyleInspector)),
      new PaneItem(new ResourceMetadata("properties", "Designer"), new ClassFactory(PropertyInspector))
    ]);
    sidebar.selectedIndices = new <int>[0, 1];
  }

  ui function editorTabViewAdded():void {
    editorTabView.items = editorPanes;
    
    editorPanes.addEventListener(CollectionEvent.COLLECTION_CHANGE, editorPanesChangeHandler, false, -1);
  }

  override protected function get primaryLaFKey():String {
    return "ProjectView";
  }

  public function addDocument(document:Document):void {
    var paneItem:DocumentPaneItem = new DocumentPaneItem(document);
    paneItem.localizedTitle = document.file.name.substring(0, document.file.name.lastIndexOf("."));

    document.tabIndex = editorPanes.size;
    editorPanes.addItem(paneItem);
  }

  private function editorPanesChangeHandler(event:CollectionEvent):void {
    if (event.kind == CollectionEventKind.REMOVE) {
      _documentFactoryManager.unregister(DocumentPaneItem(event.items[0]).document);
    }
  }
}
}

import cocoa.pane.PaneItem;

import com.intellij.flex.uiDesigner.Document;

class DocumentPaneItem extends PaneItem {
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
