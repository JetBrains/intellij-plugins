package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.plaf.ButtonSkinInteraction;
import cocoa.plaf.TableViewSkin;
import cocoa.plaf.basic.OpenedEditorInfo;
import cocoa.plaf.basic.TableViewInteractor;
import cocoa.renderer.CheckBoxEntry;
import cocoa.renderer.TextLineEntry;
import cocoa.tableView.TableColumn;
import cocoa.tableView.TableView;
import cocoa.util.SharedPoint;

import com.intellij.flex.uiDesigner.PlatformDataKeys;
import com.intellij.flex.uiDesigner.Project;

import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataManager;

public class PropertyTableInteractor extends TableViewInteractor {
  private var tableSkin:TableViewSkin;

  private var currentRowIndex:int;
  private var currentColumnIndex:int;

  private var isOver:Boolean;
  private var valueRendererManager:ValueRendererManager;

  public function PropertyTableInteractor(tableView:TableView, valueRendererManager:ValueRendererManager) {
    this.valueRendererManager = valueRendererManager;

    tableSkin = TableViewSkin(tableView.skin);
    var bodyHitArea:Sprite = tableSkin.bodyHitArea;
    register(tableView);
    bodyHitArea.addEventListener(MouseEvent.MOUSE_DOWN, mouseEventHandler);
    bodyHitArea.addEventListener(MouseEvent.DOUBLE_CLICK, mouseEventHandler);
  }

  private function mouseEventHandler(event:MouseEvent):void {
    var x:Number = event.localX;
    var y:Number = event.localY;
    if (event.target != tableSkin.bodyHitArea) {
      var point:Point = SharedPoint.point;
      point.x = event.stageX;
      point.y = event.stageY;
      point = tableSkin.bodyHitArea.globalToLocal(point);
      x = point.x;
      y = point.y;
    }

    currentRowIndex = tableSkin.getRowIndexAt(y);
    currentColumnIndex = tableSkin.getColumnIndexAt(x);
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
          var editor:Sprite = valueRendererManager.createEditor(currentRowIndex, entry, tableColumn.actualWidth, tableView.rowHeight);
          if (editor != null) {
            openedEditorInfo = new OpenedEditorInfo(editor, currentColumnIndex, currentRowIndex);
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

    var point:Point = SharedPoint.point;
    point.x = event.stageX;
    point.y = event.stageY;

    point = tableSkin.bodyHitArea.globalToLocal(point);
    var rowIndex:int = tableSkin.getRowIndexAt(point.y);
    var columnIndex:int = tableSkin.getColumnIndexAt(point.x);

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

  override protected function closeAndCommit():void {
    var entry:TextLineEntry = valueRendererManager.findEntry(openedEditorInfo.rowIndex);
    //var value:String = EditableTextView(openedEditorInfo.editor).text;
    var value:String = "";
    var tableView:TableView = TableView(tableSkin.component);
    var tableColumn:TableColumn = tableView.columns[openedEditorInfo.columnIndex];
    valueRendererManager.closeEditorAndCommit(openedEditorInfo.editor, value, entry, tableColumn.actualWidth);

    var dataContext:DataContext = DataManager.instance.getDataContext(DisplayObject(tableSkin));
    Modifier(Project(PlatformDataKeys.PROJECT.getData(dataContext)).getComponent(Modifier)).applyString(valueRendererManager.getDescription(openedEditorInfo.rowIndex),
                                                                                                        value, dataContext);
  }

  override protected function closeAndRollback():void {
    valueRendererManager.closeEditor(openedEditorInfo.editor);
  }
}
}