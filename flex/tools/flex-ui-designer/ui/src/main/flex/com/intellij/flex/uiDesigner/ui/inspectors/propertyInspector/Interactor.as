package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.plaf.TableViewSkin;
import cocoa.plaf.basic.ButtonSkinInteraction;
import cocoa.tableView.TableView;
import cocoa.tableView.TextLineLinkedListEntry;

import flash.events.MouseEvent;
import flash.geom.Point;

public class Interactor {
  private static var sharedPoint:Point;

  private var modifier:Modifier;

  private var tableView:TableView;
  private var tableSkin:TableViewSkin;

  private var currentRowIndex:int;
  private var currentColumnIndex:int;

  private var isOver:Boolean;

  public function Interactor(tableView:TableView, modifier:Modifier) {
    this.tableView = tableView;
    this.modifier = modifier;

    tableSkin = TableViewSkin(tableView.skin);
    tableSkin.bodyHitArea.addEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
  }

  private function mouseDownHandler(event:MouseEvent):void {
    //if (element != null) {
    //  mouseDownOnElement = true;
    //}

    currentRowIndex = tableSkin.getRowIndexAt(event.localY);
    currentColumnIndex = tableSkin.getColumnIndexAt(event.localX);
    if (currentColumnIndex == 1) {
      var entry:TextLineLinkedListEntry = findEntry();
      if (entry is CheckBoxLinkedListEntry) {
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
    }
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

  private function findEntry():TextLineLinkedListEntry {
    return ValueRendererManager(tableView.columns[1]).findEntry(currentRowIndex);
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

    var entry:CheckBoxLinkedListEntry = CheckBoxLinkedListEntry(findEntry());
    if (rowIndex == currentRowIndex && columnIndex == currentColumnIndex) {
      entry.interaction.mouseUpHandler(event);

      modifier.applyBoolean(ValueRendererManager(tableView.columns[columnIndex]).getDescription(rowIndex), entry.checkbox.selected);
    }

    resetCurrentIndices();
  }

  private function getButtonSkinInteraction():ButtonSkinInteraction {
    return CheckBoxLinkedListEntry(findEntry()).interaction;
  }
}
}
