package com.intellij.flex.uiDesigner.flex {
import flash.display.DisplayObject;
import flash.geom.Point;
import flash.geom.Rectangle;

import mx.core.ILayoutDirectionElement;
import mx.core.IUIComponent;
import mx.core.LayoutDirection;
import mx.core.mx_internal;
import mx.managers.ISystemManager;
import mx.managers.IToolTipManager2;
import mx.managers.ToolTipManagerImpl;
import mx.styles.IStyleClient;

use namespace mx_internal;

// IDEA-72677, flex cannot properly calculate, because our system manager doesn't have 0, 0 position
public class ToolTipManager extends ToolTipManagerImpl {
  private static var instance:ToolTipManager;

  public static function getInstance():IToolTipManager2 {
    if (instance == null) {
      instance = new ToolTipManager();
    }

    return instance;
  }

  private static function getGlobalBounds(obj:DisplayObject, parent:DisplayObject, mirror:Boolean):Rectangle {
    var offset:Point = SystemManager(IUIComponent(obj).systemManager).localToGlobal(new Point(0, 0));
    var upperLeft:Point = obj.localToGlobal(new Point(0, 0));
    // If the layout has been mirrored, then the 0,0 is the uppper right corner; compensate here
    if (mirror) {
      upperLeft.x -= obj.width;
    }

    upperLeft = parent.globalToLocal(upperLeft);
    return new Rectangle(upperLeft.x - offset.x, upperLeft.y - offset.y, obj.width, obj.height);
  }

  override mx_internal function positionTip():void {
    // Determine layoutDirection of the target component.
    var layoutDirection:String;
    if (currentTarget is ILayoutDirectionElement) {
      layoutDirection = ILayoutDirectionElement(currentTarget).layoutDirection;
    }
    else {
      layoutDirection = LayoutDirection.LTR;
    }

    const mirror:Boolean = (layoutDirection == LayoutDirection.RTL);

    var x:Number;
    var y:Number;

    var screenWidth:Number = currentToolTip.screen.width;
    var screenHeight:Number = currentToolTip.screen.height;

    if (isError) {
      // Tooltips are laid out in the same direction as the target
      // component.
      var tipElt:ILayoutDirectionElement =
        currentToolTip as ILayoutDirectionElement;

      if (tipElt &&
          tipElt.layoutDirection != layoutDirection) {
        tipElt.layoutDirection = layoutDirection;
        // sizeTip below will call validateNow()
        tipElt.invalidateLayoutDirection();
      }

      var targetGlobalBounds:Rectangle =
        getGlobalBounds(currentTarget, currentToolTip.root, mirror);

      x = mirror ?
          targetGlobalBounds.left - 4 :
          targetGlobalBounds.right + 4;
      y = targetGlobalBounds.top - 1;

      // If there's no room to the right (left) of the control, put it
      // above or below, with the left (right) edge of the error tip
      // aligned with the left (right) edge of the target.
      var noRoom:Boolean = mirror ?
                           x < currentToolTip.width :
                           x + currentToolTip.width > screenWidth;
      if (noRoom) {
        var newWidth:Number = NaN;
        var oldWidth:Number = NaN;

        x = mirror ?
            targetGlobalBounds.right + 2 - currentToolTip.width :
            targetGlobalBounds.left - 2;

        // If the error tip would be too wide for the stage,
        // reduce the maximum width to fit onstage. Note that
        // we have to reassign the text in order to get the tip
        // to relayout after changing the border style and maxWidth.
        if (mirror) {
          if (x < currentToolTip.width + 4) {
            // -4 on the left, +2 on the right = -2
            x = 4;
            newWidth = targetGlobalBounds.right - 2;
          }
        }
        else {
          if (x + currentToolTip.width + 4 > screenWidth) {
            newWidth = screenWidth - x - 4;
          }
        }

        if (!isNaN(newWidth)) {
          oldWidth = Object(toolTipClass).maxWidth;
          Object(toolTipClass).maxWidth = newWidth;
          if (currentToolTip is IStyleClient) {
            IStyleClient(currentToolTip).setStyle("borderStyle", "errorTipAbove");
          }
          currentToolTip["text"] = currentToolTip["text"];
        }

        // Even if the error tip will fit onstage, we still need to
        // change the border style and get the error tip to relayout.
        else {
          if (currentToolTip is IStyleClient) {
            IStyleClient(currentToolTip).setStyle("borderStyle", "errorTipAbove");
          }
          currentToolTip["text"] = currentToolTip["text"];
        }

        if (currentToolTip.height + 2 < targetGlobalBounds.top) {
          // There's room to put it above the control.
          y = targetGlobalBounds.top - (currentToolTip.height + 2);
        }
        else {
          // No room above, put it below the control.
          y = targetGlobalBounds.bottom + 2;

          if (!isNaN(newWidth)) {
            Object(toolTipClass).maxWidth = newWidth;
          }
          if (currentToolTip is IStyleClient) {
            IStyleClient(currentToolTip).setStyle("borderStyle", "errorTipBelow");
          }
          currentToolTip["text"] = currentToolTip["text"];
        }
      }

      // Since the border style of the error tip may have changed,
      // we have to force a remeasurement and change its size.
      // This is because objects in the toolTips layer
      // don't undergo normal measurement and layout.
      sizeTip(currentToolTip);

      // If we changed the tooltip max size, we change it back.
      // Otherwise, if RTL, and x wasn't set for maxWidth, reposition
      // because the width may have changed during the remeasure.
      if (!isNaN(oldWidth)) {
        Object(toolTipClass).maxWidth = oldWidth;
      }
      else if (mirror) {
        x = targetGlobalBounds.right + 2 - currentToolTip.width;
      }
    }
    else {
      var sm:ISystemManager = getSystemManager(currentTarget);
      // Position the upper-left (upper-right) of the tooltip
      // at the lower-right (lower-left) of the arrow cursor.
      x = DisplayObject(sm).mouseX + 11;
      if (mirror) {
        x -= currentToolTip.width;
      }
      y = DisplayObject(sm).mouseY + 22;

      // If the tooltip is too wide to fit onstage, move it left (right).
      var toolTipWidth:Number = currentToolTip.width;
      if (mirror) {
        if (x < 2) {
          x = 2;
        }
      }
      else if (x + toolTipWidth > screenWidth) {
        x = screenWidth - toolTipWidth;
      }

      // If the tooltip is too tall to fit onstage, move it up.
      var toolTipHeight:Number = currentToolTip.height;
      if (y + toolTipHeight > screenHeight) {
        y = screenHeight - toolTipHeight;
      }

      var pos:Point = new Point(x, y);
      pos = DisplayObject(sm).localToGlobal(pos);
      pos = DisplayObject(sm.getSandboxRoot()).globalToLocal(pos);
      x = pos.x;
      y = pos.y;
    }

    currentToolTip.move(x, y);

  }
}
}
