package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.plaf.ButtonSkinInteraction;
import cocoa.plaf.TableViewSkin;
import cocoa.plaf.basic.TableViewInteractor;
import cocoa.renderer.CheckBoxEntry;
import cocoa.renderer.TextLineEntry;
import cocoa.tableView.TableColumn;
import cocoa.tableView.TableView;

import com.intellij.flex.uiDesigner.PlatformDataKeys;
import com.intellij.flex.uiDesigner.Project;

import flash.display.DisplayObject;
import flash.display.InteractiveObject;
import flash.display.Sprite;
import flash.events.FocusEvent;
import flash.events.MouseEvent;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataManager;

public class Interactor extends TableViewInteractor {
  private static var sharedPoint:Point;

  private var tableSkin:TableViewSkin;

  private var currentRowIndex:int;
  private var currentColumnIndex:int;

  private var isOver:Boolean;
  private var valueRendererManager:ValueRendererManager;

  public function Interactor(tableView:TableView, valueRendererManager:ValueRendererManager) {
    this.valueRendererManager = valueRendererManager;

    tableSkin = TableViewSkin(tableView.skin);
    var bodyHitArea:Sprite = tableSkin.bodyHitArea;
    register(tableView);
    bodyHitArea.addEventListener(MouseEvent.MOUSE_DOWN, mouseEventHandler);
    bodyHitArea.addEventListener(MouseEvent.DOUBLE_CLICK, mouseEventHandler);
  }

  private function mouseEventHandler(event:MouseEvent):void {
    currentRowIndex = tableSkin.getRowIndexAt(event.localY);
    currentColumnIndex = tableSkin.getColumnIndexAt(event.localX);
    if (currentColumnIndex == 1) {
      var entry:TextLineEntry = findEntry();
      if (event.type == MouseEvent.MOUSE_DOWN) {
        if (entry is CheckBoxEntry) {
          mouseDownOnButton(event);
        }
      }
      else if (event.type == MouseEvent.DOUBLE_CLICK) {
        var tableView:TableView = TableView(tableSkin.component);
        var tableColumn:TableColumn = tableView.columns[currentColumnIndex];
        if (tableColumn.rendererManager == valueRendererManager) {
          openedEditor = valueRendererManager.createEditor(currentRowIndex, entry, tableColumn.actualWidth, tableView.rowHeight);
          if (openedEditor != null) {
            registerEditor();
          }
        }
      }
    }
  }

  private function mouseDownOnButton(event:MouseEvent):void {
    var skin:ButtonSkinInteraction = getButtonSkinInteraction();
    if (!skin.enabled) {
      resetCurrentIndices();
      return;
    }

    skin.mouseDownHandler(event);

    isOver = true;

    tableSkin.bodyHitArea.stage.addEventListener(MouseEvent.MOUSE_UP, stageMouseUpHandler);
    tableSkin.bodyHitArea.addEventListener(MouseEvent.MOUSE_MOVE, mouseMoveHandler);
    tableSkin.bodyHitArea.addEventListener(MouseEvent.ROLL_OUT, mouseRollOutHandler);
  }

  private function mouseRollOutHandler(event:MouseEvent):void {
    if (isOver) {
      isOver = false;
      getButtonSkinInteraction().mouseOutHandler(event);
    }
  }

  private function resetCurrentIndices():void {
    currentRowIndex = -1;
    currentColumnIndex = -1;
  }

  private function mouseMoveHandler(event:MouseEvent):void {
    var rowIndex:int = tableSkin.getRowIndexAt(event.localY);
    var columnIndex:int = tableSkin.getColumnIndexAt(event.localX);
    if (rowIndex == currentRowIndex && columnIndex == currentColumnIndex) {
      if (!isOver) {
        isOver = true;
        getButtonSkinInteraction().mouseOverHandler(event);
      }
    }
    else if (isOver) {
      isOver = false;
      getButtonSkinInteraction().mouseOutHandler(event);
    }
  }

  private function findEntry():TextLineEntry {
    return valueRendererManager.findEntry(currentRowIndex);
  }

  private function stageMouseUpHandler(event:MouseEvent):void {
    tableSkin.bodyHitArea.stage.removeEventListener(MouseEvent.MOUSE_UP, stageMouseUpHandler);
    tableSkin.bodyHitArea.removeEventListener(MouseEvent.MOUSE_MOVE, mouseMoveHandler);
    tableSkin.bodyHitArea.removeEventListener(MouseEvent.ROLL_OUT, mouseRollOutHandler);

    if (sharedPoint == null) {
      sharedPoint = new Point(event.stageX, event.stageY);
    }
    else {
      sharedPoint.x = event.stageX;
      sharedPoint.y = event.stageY;
    }

    sharedPoint = tableSkin.bodyHitArea.globalToLocal(sharedPoint);
    var rowIndex:int = tableSkin.getRowIndexAt(sharedPoint.y);
    var columnIndex:int = tableSkin.getColumnIndexAt(sharedPoint.x);

    var entry:CheckBoxEntry = CheckBoxEntry(findEntry());
    if (rowIndex == currentRowIndex && columnIndex == currentColumnIndex) {
      entry.interaction.mouseUpHandler(event);

      var dataContext:DataContext = DataManager.instance.getDataContext(DisplayObject(tableSkin));
      Modifier(Project(PlatformDataKeys.PROJECT.getData(dataContext)).getComponent(Modifier)).applyBoolean(valueRendererManager.getDescription(rowIndex), entry.checkbox.selected, dataContext);
    }

    resetCurrentIndices();
  }

  private function getButtonSkinInteraction():ButtonSkinInteraction {
    return CheckBoxEntry(findEntry()).interaction;
  }

  override protected function closeEditor(commit:Boolean):void {
    super.closeEditor(commit);

    valueRendererManager.closeEditor(openedEditor);
  }
}
}