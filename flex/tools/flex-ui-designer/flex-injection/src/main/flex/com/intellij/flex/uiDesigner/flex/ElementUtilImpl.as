package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.css.StyleDeclarationProxy;

import flash.display.BlendMode;
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Stage;
import flash.geom.Point;
import flash.system.ApplicationDomain;
import flash.utils.getQualifiedClassName;

import mx.core.Container;

import mx.core.ILayoutElement;
import mx.core.IUIComponent;
import mx.core.IVisualElement;
import mx.core.UIComponent;
import mx.core.mx_internal;

import spark.components.Group;
import spark.components.supportClasses.Skin;
import spark.core.IGraphicElement;
import spark.primitives.supportClasses.GraphicElement;

use namespace mx_internal;

internal final class ElementUtilImpl implements ElementUtil {
  private static const sharedPoint:Point = new Point();
  private static const MX_CORE_UITEXTFIELD:String = "mx.core.UITextField";
  private static const SKINNABLE_CONTAINER:String = "spark.components.SkinnableContainer";

  private static var _instance:ElementUtilImpl;
  internal static function get instance():ElementUtil {
    if (_instance == null) {
      _instance = new ElementUtilImpl();
    }
    return _instance;
  }

  public function getObjectUnderPoint(stage:Stage, stageX:Number, stageY:Number):Object {
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

    while (object != null &&
           (object is Skin || (uiTextFieldClass != null && object is uiTextFieldClass) || !(object is IVisualElement) ||
            Object(object).constructor == UIComponent) /* mx skins — if object concrete type equals UIComponent, so, it is skin part */) {
      object = object.parent;
    }

    var uiComponent:IUIComponent = object as IUIComponent;
    if (uiComponent != null) {
      var skinnableContainerClass:Class = getSkinnableContainerClass();
      object = skipSkinChrome(uiComponent, skinnableContainerClass, false);
    }

    // if we click on IGraphicElement and group shares it's display object, our event target will be group instead of IGraphicElement and getObjectsUnderPoint never return this IGraphicElement,
    // so, we need find IGraphicElement by mouse click point
    if (object is Group) {
      var group:Group = Group(object);
      var local:Point = group.globalToLocal(sharedPoint);
      var numElements:int = group.numElements;
      // can share display object? we cannot check layeringMode (private)
      if (numElements > 0 && group.scrollRect == null && (group.blendMode == BlendMode.NORMAL || group.blendMode == "auto") &&
          (group.alpha == 0 || group.alpha == 1)) {
        // from end to begin, according to flash player layering
        for (var i:int = numElements - 1; i > -1; i--) {
          var graphicElement:IGraphicElement = group.getElementAt(i) as IGraphicElement;
          // topObjectUnderPoint will be spark.components.supportClasses.InvalidatingSprite or this group
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

    // IDEA-72691
    if (object != null && currentDomain.hasDefinition("mx.controls.scrollClasses.ScrollBar")) {
      if (object.parent is Class(currentDomain.getDefinition("mx.controls.scrollClasses.ScrollBar"))) {
        var p:DisplayObjectContainer = object.parent.parent;
        return p is SystemManagerSB ? object.parent : p;
      }
    }

    return object;
  }

  // for element tree bar strict must be true — isSkinnableContainerContent for contentGroup itself must return false,
  // but for getObjectUnderPoint must return true, because we need to find IGraphicElements
  private static function skipSkinChrome(object:IUIComponent, skinnableContainerClass:Class, strict:Boolean):DisplayObject {
    var document:Object;
    var uiComponent:IUIComponent = object;
    while (documentIsSkin((document = uiComponent.document)) &&
           !isSkinnableContainerContent(skinnableContainerClass, document, uiComponent, strict)) {
      object = document.parent;
      if ((uiComponent = object as IUIComponent) == null) {
        break;
      }
    }

    return DisplayObject(object);
  }

  private static function getSkinnableContainerClass():Class {
    var skinnableContainerClass:Class;
    var currentDomain:ApplicationDomain = ApplicationDomain.currentDomain;
    if (currentDomain.hasDefinition(SKINNABLE_CONTAINER)) {
      skinnableContainerClass = Class(currentDomain.getDefinition(SKINNABLE_CONTAINER));
    }
    return skinnableContainerClass;
  }

  // IDEA-71968, Skin as root document
  private static function documentIsSkin(object:Object):Boolean {
    return object is Skin && !(Skin(object).parent is SystemManagerSB);
  }

  // see Panel title="One" in MouseSelectionTest. click on panel title — select panel, but click on panel content element Label — select this Label
  private static function isSkinnableContainerContent(skinnableContainerClass:Class, document:Object, uiComponent:IUIComponent, strict:Boolean):Boolean {
    if (skinnableContainerClass == null || !("hostComponent" in document) || !(document.hostComponent is skinnableContainerClass)) {
      return false;
    }

    var group:Group = document.hostComponent.contentGroup;
    return group != null && (!strict || group != uiComponent) && group.contains(DisplayObject(uiComponent));
  }

  public function fillBreadcrumbs(element:Object, source:Vector.<String>):int {
    var count:int;
    var skinnableContainerClass:Class = getSkinnableContainerClass();
    do {
      var uiComponent:IUIComponent = element as IUIComponent;
      if (uiComponent != null) {
        element = skipSkinChrome(uiComponent, skinnableContainerClass, true);
      }

      var qualifiedClassName:String = getQualifiedClassName(element);
      source[count++] = qualifiedClassName.substr(qualifiedClassName.lastIndexOf("::") + 2);
    }
    while (!((element = element.parent) is SystemManagerSB));

    return count;
  }

  public function getSize(element:Object, result:Point):void {
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

  public function getPosition(element:Object, result:Point):Point {
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

  public function getDisplayObject(o:Object):DisplayObject {
    return o is DisplayObject ? DisplayObject(o) : GraphicElement(o).owner;
  }
}
}