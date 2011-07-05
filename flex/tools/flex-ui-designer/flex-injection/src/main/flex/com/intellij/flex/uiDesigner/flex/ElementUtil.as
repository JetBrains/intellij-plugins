package com.intellij.flex.uiDesigner.flex {
import flash.display.BlendMode;
import flash.display.DisplayObject;
import flash.display.Stage;
import flash.geom.Point;
import flash.system.ApplicationDomain;

import mx.core.ILayoutElement;
import mx.core.IUIComponent;
import mx.core.IVisualElement;
import mx.core.UIComponent;
import mx.core.mx_internal;

import spark.components.Group;
import spark.components.supportClasses.Skin;
import spark.core.IGraphicElement;

use namespace mx_internal;

public final class ElementUtil {
  private static const sharedPoint:Point = new Point();
  private static const MX_CORE_UITEXTFIELD:String = "mx.core.UITextField";
  private static const SPARK_COMPONENTS_SKINNABLECONTAINER:String = "spark.components.SkinnableContainer";

  //noinspection JSUnusedGlobalSymbols
  public static function getObjectUnderPoint(stage:Stage, stageX:Number, stageY:Number):Object {
    sharedPoint.x = stageX;
		sharedPoint.y = stageY;

    var objectsUnderPoint:Array = stage.getObjectsUnderPoint(sharedPoint);
    if (objectsUnderPoint.length == 0) {
      return null;
    }

    const topObjectUnderPoint:DisplayObject = objectsUnderPoint[objectsUnderPoint.length - 1];
    var object:DisplayObject = topObjectUnderPoint;

    var currentDomain:ApplicationDomain = ApplicationDomain.currentDomain;
    var uiTextFieldClass:Class;
    if (currentDomain.hasDefinition(MX_CORE_UITEXTFIELD)) {
      uiTextFieldClass = Class(currentDomain.getDefinition(MX_CORE_UITEXTFIELD));
    }

    var skinnableContainerClass:Class;
    if (currentDomain.hasDefinition(SPARK_COMPONENTS_SKINNABLECONTAINER)) {
      skinnableContainerClass = Class(currentDomain.getDefinition(SPARK_COMPONENTS_SKINNABLECONTAINER));
    }

    while (object != null &&
            (object is Skin || (uiTextFieldClass != null && object is uiTextFieldClass) || !(object is IVisualElement) ||
            Object(object).constructor == UIComponent) /* mx skins — if object concrete type equals UIComponent, so, it is skin part */) {
      object = object.parent;
    }

    var uiComponent:IUIComponent = object as IUIComponent;
    if (uiComponent != null) {
      var document:Object;
      while ((document = uiComponent.document) is Skin && !isSkinnableContainerContent(skinnableContainerClass, document, uiComponent)) {
        object = document.parent;
        if ((uiComponent = object as IUIComponent) == null) {
          break;
        }
      }
    }

    // if we click on IGraphicElement and group shares it's display object, our event target will be grop instead of IGraphicElement and getObjectsUnderPoint never return this IGraphicElement,
    // so, we need find IGraphicElement by mouse click point
    if (object is Group) {
      var group:Group = Group(object);
      var local:Point = group.globalToLocal(sharedPoint);
      var numElements:int = group.numElements;
      // can share display object? we cannot check layeringMode (private)
      if (numElements > 0 && group.scrollRect == null && (group.blendMode == BlendMode.NORMAL || group.blendMode == "auto") && (group.alpha == 0 || group.alpha == 1)) {
        // from end to begin, according to flash player layering
        for (var i:int = numElements - 1; i > -1; i--) {
          var graphicElement:IGraphicElement = group.getElementAt(i) as IGraphicElement;
          // topObjectUnderPoint will be spark.components.supportClasses.InvalidatingSprite
          if (graphicElement != null && graphicElement.displayObject == topObjectUnderPoint) {
            const ex:Number = graphicElement.getLayoutBoundsX();
            if (local.x >= ex && local.x <= (ex + graphicElement.getLayoutBoundsWidth())) {
              const ey:Number = graphicElement.getLayoutBoundsY();
              if (local.y >= ey && local.y <= (ey + graphicElement.getLayoutBoundsHeight())) {
                return graphicElement;
              }
            }
          }
        }
      }
    }

    return object;
  }

  // see Panel title="One" in MouseSelectionTest. click on panel title — select panel, but click on panel content element Label — select this Label
  private static function isSkinnableContainerContent(skinnableContainerClass:Class, document:Object, uiComponent:IUIComponent):Boolean {
    if (skinnableContainerClass == null || !("hostComponent" in document) || !(document.hostComponent is skinnableContainerClass)) {
      return false;
    }

    var group:Group = document.hostComponent.contentGroup;
    return group != null && group.contains(DisplayObject(uiComponent));
  }

  //noinspection JSUnusedGlobalSymbols
  public static function getSize(element:Object, result:Point):void {
    var layoutElement:ILayoutElement = element as ILayoutElement;
    if (layoutElement != null) {
      result.x = layoutElement.getLayoutBoundsWidth();
      result.y = layoutElement.getLayoutBoundsHeight();
    }
    else {
      var uiComponent:IUIComponent = element as IUIComponent;
      if (uiComponent != null) {
        result.x = uiComponent.getExplicitOrMeasuredWidth();
        result.y = uiComponent.getExplicitOrMeasuredHeight();
      }
      else {
        var displayObject:DisplayObject = DisplayObject(element);
        result.x = displayObject.width;
        result.y = displayObject.height;
      }
    }
  }

  //noinspection JSUnusedGlobalSymbols
  public static function getPosition(element:Object, result:Point):Point {
    var layoutElement:ILayoutElement = element as ILayoutElement;
    if (layoutElement != null) {
      result.x = layoutElement.getLayoutBoundsX();
      result.y = layoutElement.getLayoutBoundsY();
    }
    else {
      var displayObject:DisplayObject = DisplayObject(element);
      result.x = displayObject.x;
      result.y = displayObject.y;
    }

    // IDEA-71787, mx Button in mx Panel — actual parent is not Panel, but FlexSprite
    return (element is UIComponent ? UIComponent(element).$parent : element.parent).localToGlobal(result);
  }
}
}