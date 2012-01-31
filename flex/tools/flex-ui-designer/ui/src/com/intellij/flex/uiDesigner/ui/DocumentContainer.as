package com.intellij.flex.uiDesigner.ui {
import cocoa.ContentView;
import cocoa.ControlView;
import cocoa.plaf.LookAndFeel;

import com.intellij.flex.uiDesigner.DocumentDisplayManager;
import com.intellij.flex.uiDesigner.DocumentFactory;
import com.intellij.flex.uiDesigner.designSurface.AreaLocations;
import com.intellij.flex.uiDesigner.designSurface.DesignSurfaceDataKeys;
import com.intellij.flex.uiDesigner.designSurface.ToolManager;
import com.intellij.flex.uiDesigner.designSurface.ViewersComposite;
import com.intellij.flex.uiDesigner.gef.core.EditDomain;
import com.intellij.flex.uiDesigner.gef.graphical.GraphicalViewer;

import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Sprite;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataKey;
import org.jetbrains.actionSystem.DataManager;

public class DocumentContainer extends ControlView implements DataContext {
  private static const MIN_CANVAS_WIDTH:int = 500;
  private static const MIN_CANVAS_HEIGHT:int = 400;

  private static const AREA_LOCATIONS:Vector.<Point> = new <Point>[new Point(), new Point(), new Point()];

  private var designerAreaOuterBackgroundColor:int;
  private var documentDisplayManager:DocumentDisplayManager;

  // min w/h compute by size of default canvas (the same as in idea/wbpro)
  // user can change size of this canvas, but it is not set document size — only canvas
  // default value the same as in idea (wbpro has another — 450x300)
  private var canvasWidth:int = MIN_CANVAS_WIDTH;
  private var canvasHeight:int = MIN_CANVAS_HEIGHT;

  public function DocumentContainer(documentSystemManager:DocumentDisplayManager) {
    this.documentDisplayManager = documentSystemManager;
  }

  override public function getMinimumWidth(hHint:int = -1):int {
    return getPreferredWidth();
  }

  override public function getMinimumHeight(wHint:int = -1):int {
    return getPreferredHeight();
  }

  override public function getPreferredWidth(hHint:int = -1):int {
    return canvasWidth + CANVAS_INSET;
  }

  override public function getPreferredHeight(wHint:int = -1):int {
    return canvasHeight + CANVAS_INSET;
  }

  override public function addToSuperview(displayObjectContainer:DisplayObjectContainer, laf:LookAndFeel, superview:ContentView = null):void {
    var viewersComposite:ViewersComposite = new ViewersComposite();
    viewersComposite.addToSuperview(displayObjectContainer, laf, superview);

    var viewer:GraphicalViewer = viewersComposite.viewer;
    viewer.editDomain = new EditDomain();
    //viewer.editPartFactory = EditPartFactory.INSTANCE;

    viewersComposite.bindViewers();

    super.addToSuperview(displayObjectContainer, laf, superview);

    designerAreaOuterBackgroundColor = laf.getInt("designerAreaOuterBackgroundColor");

    var toolManager:ToolManager = ToolManager(DocumentFactory(documentDisplayManager.documentFactory).module.project.getComponent(ToolManager));
    if (numChildren > 0 && documentDisplayManager == getChildAt(0)) {
      return;
    }

    toolManager.displayObjectContainer = displayObjectContainer;
    toolManager.areaLocations = AREA_LOCATIONS;

    var s:Sprite = Sprite(documentDisplayManager);
    s.x = CANVAS_INSET;
    s.y = CANVAS_INSET;
    addChild(s);
    documentDisplayManager.added();

    if (documentDisplayManager.preferredDocumentWidth != -1) {
      canvasWidth = documentDisplayManager.preferredDocumentWidth;
    }
    else {
      canvasWidth = Math.max(MIN_CANVAS_WIDTH, documentDisplayManager.minDocumentWidth);
    }
    
    if (documentDisplayManager.preferredDocumentHeight != -1) {
      canvasHeight = documentDisplayManager.preferredDocumentHeight;
    }
    else {
      canvasWidth = Math.max(MIN_CANVAS_HEIGHT, documentDisplayManager.minDocumentHeight);
    }
  }

  override protected function draw(w:int, h:int):void {
    DocumentDisplayManager(documentDisplayManager).setDocumentBounds(canvasWidth, canvasHeight);
  }

  public function getData(dataKey:DataKey):Object {
    switch (dataKey) {
      case DesignSurfaceDataKeys.DOCUMENT_DISPLAY_MANAGER:
        return documentDisplayManager;

      default:
        return DataManager.instance.getDataContext(parent).getData(dataKey);
    }
  }
}
}