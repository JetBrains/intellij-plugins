package com.intellij.flex.uiDesigner.ui {
import cocoa.ContentView;
import cocoa.ControlView;
import cocoa.plaf.LookAndFeel;

import com.intellij.flex.uiDesigner.DocumentDisplayManager;
import com.intellij.flex.uiDesigner.DocumentFactory;
import com.intellij.flex.uiDesigner.designSurface.AreaLocations;
import com.intellij.flex.uiDesigner.designSurface.DesignSurfaceDataKeys;
import com.intellij.flex.uiDesigner.designSurface.ToolManager;

import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Sprite;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataKey;
import org.jetbrains.actionSystem.DataManager;

public class DocumentContainer extends ControlView implements DataContext {
  private static const HEADER_SIZE:int = 15;
  private static const CANVAS_INSET:int = HEADER_SIZE + 1 /* line thickness */ + 20;

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

  private var sizeByDocumentInvalid:Boolean = true;

  public function DocumentContainer(documentSystemManager:DocumentDisplayManager) {
    this.documentDisplayManager = documentSystemManager;
  }

  override public function getMinimumWidth(hHint:int = -1):int {
    measureByDocument();
    //return getPreferredWidth();
    return CANVAS_INSET;
  }

  override public function getMinimumHeight(wHint:int = -1):int {
    measureByDocument();
    //return getPreferredHeight();
    return CANVAS_INSET;
  }

  override public function getPreferredWidth(hHint:int = -1):int {
    measureByDocument();
    return canvasWidth + CANVAS_INSET;
  }

  override public function getPreferredHeight(wHint:int = -1):int {
    measureByDocument();
    return canvasHeight + CANVAS_INSET;
  }

  override public function setLocation(x:Number, y:Number):void {
    super.setLocation(x, y);

    AREA_LOCATIONS[AreaLocations.HORIZONTAL_HEADER].x = x;
    AREA_LOCATIONS[AreaLocations.HORIZONTAL_HEADER].y = y + HEADER_SIZE + 1;

    AREA_LOCATIONS[AreaLocations.VERTICAL_HEADER].x = x + HEADER_SIZE + 1;
    AREA_LOCATIONS[AreaLocations.VERTICAL_HEADER].y = y;

    AREA_LOCATIONS[AreaLocations.BODY].x = x + CANVAS_INSET;
    AREA_LOCATIONS[AreaLocations.BODY].y = y + CANVAS_INSET;
  }

  public function documentUpdated():void {
    sizeByDocumentInvalid = true;
    invalidate(true);
  }

  override public function addToSuperview(displayObjectContainer:DisplayObjectContainer, laf:LookAndFeel, superview:ContentView = null):void {
    //var viewersComposite:ViewersComposite = new ViewersComposite();
    //viewersComposite.addToSuperview(displayObjectContainer, laf, superview);
    //
    //var viewer:GraphicalViewer = viewersComposite.viewer;
    //viewer.editDomain = new EditDomain();
    ////viewer.editPartFactory = EditPartFactory.INSTANCE;
    //
    //viewersComposite.bindViewers();

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
  }

  private function measureByDocument():void {
    if (!sizeByDocumentInvalid) {
      return;
    }

    sizeByDocumentInvalid = false;

    if (documentDisplayManager.explicitDocumentWidth != -1) {
      canvasWidth = documentDisplayManager.explicitDocumentWidth;
    }
    else {
      canvasWidth = Math.max(MIN_CANVAS_WIDTH, documentDisplayManager.minDocumentWidth);
    }

    if (documentDisplayManager.explicitDocumentHeight != -1) {
      canvasHeight = documentDisplayManager.explicitDocumentHeight;
    }
    else {
      canvasHeight = Math.max(MIN_CANVAS_HEIGHT, documentDisplayManager.minDocumentHeight);
    }
  }

  override protected function draw(w:int, h:int):void {
    DocumentDisplayManager(documentDisplayManager).setDocumentBounds(canvasWidth, canvasHeight);

    var g:Graphics = graphics;
    g.clear();

    // all insets the same as in idea

    // draw horizontal and vertical headers background
    // color from wbpro, it is better than idea (because idea is more dark) (I think)
    g.beginFill(designerAreaOuterBackgroundColor);
    g.lineTo(w, 0);
    g.lineTo(w, HEADER_SIZE);
    // color from wbpro, it is better than idea (because idea is more dark) (I think)
    g.lineStyle(1, 0x9f9f9f);
    g.lineTo(HEADER_SIZE, HEADER_SIZE);
    g.lineTo(HEADER_SIZE, h);
    g.lineStyle();
    g.lineTo(0, h);
    g.lineTo(0, 0);
    g.endFill();

    // draw inner white rectangle
    g.beginFill(0xffffff);
    const canvasBackgroundInset:int = HEADER_SIZE + 1;
    g.drawRect(canvasBackgroundInset, canvasBackgroundInset, w - canvasBackgroundInset, h - canvasBackgroundInset);
    g.endFill();
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