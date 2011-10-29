package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentDisplayManager;
import com.intellij.flex.uiDesigner.ElementInfoProvider;
import com.intellij.flex.uiDesigner.ResourceBundleProvider;
import com.intellij.flex.uiDesigner.UiErrorHandler;

import flash.display.DisplayObject;
import flash.display.Stage;
import flash.system.ApplicationDomain;

public class FlashDocumentDisplayManager extends AbstractDocumentDisplayManager implements DocumentDisplayManager {
  public function get elementUtil():ElementInfoProvider {
    return FlashElementInfoProvider.instance;
  }

  public function get sharedInitialized():Boolean {
    return true;
  }

  public function getDefinitionByName(name:String):Object {
    return ApplicationDomain.currentDomain.getDefinition(name);
  }

  public function initShared(stage:Stage, project:Object, resourceBundleProvider:ResourceBundleProvider,
                               uiErrorHandler:UiErrorHandler):void {
  }

  override public function init(moduleFactory:Object, uiErrorHandler:UiErrorHandler,
                         mainFocusManager:MainFocusManagerSB, documentFactory:Object):void {
    super.init(moduleFactory, uiErrorHandler, mainFocusManager, documentFactory);
  }

  private static function initialExplicitDimension(dimension:Number):Number {
    return dimension == 0 || dimension != dimension ? NaN : dimension;
  }

  public function setUserDocument(object:DisplayObject):void {
    removeEventHandlers();

    if (_document != null) {
      removeChild(_document);
    }

    _document = object;

    _explicitDocumentSize.width = initialExplicitDimension(object.width);
    _explicitDocumentSize.height = initialExplicitDimension(object.height);

    try {
      addChildAt(object, 0);
    }
    catch (e:Error) {
      if (contains(_document)) {
        removeChild(_document);
      }

      _document = null;
      throw e;
    }
  }

  public function added():void {
  }

  public function activated():void {
  }

  public function deactivated():void {
  }

  public function setStyleManagerForTalentAdobeEngineers(value:Boolean):void {
  }

  public function removeEventHandlers():void {
  }

  public function get layoutManager():Object {
    return null;
  }
}
}

import com.intellij.flex.uiDesigner.DocumentDisplayManager;
import com.intellij.flex.uiDesigner.ElementInfoProvider;

import flash.display.DisplayObject;
import flash.display.Stage;
import flash.geom.Point;
import flash.utils.getQualifiedClassName;

final class FlashElementInfoProvider implements ElementInfoProvider {
  private static const sharedPoint:Point = new Point();

  private static var _instance:FlashElementInfoProvider;
  internal static function get instance():ElementInfoProvider {
    if (_instance == null) {
      _instance = new FlashElementInfoProvider();
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

    return objectsUnderPoint[objectsUnderPoint.length - 1];
  }

  public function fillBreadcrumbs(element:Object, source:Vector.<String>):int {
    var count:int;
    do {
      var qualifiedClassName:String = getQualifiedClassName(element);
      source[count++] = qualifiedClassName.substr(qualifiedClassName.lastIndexOf("::") + 2);
    }
    while (!((element = element.parent) is DocumentDisplayManager));

    return count;
  }

  public function getSize(element:Object, result:Point):void {
    var displayObject:DisplayObject = DisplayObject(element);
    result.x = displayObject.width;
    result.y = displayObject.height;
  }

  public function getPosition(element:Object, result:Point):Point {
    var displayObject:DisplayObject = DisplayObject(element);
    result.x = displayObject.x;
    result.y = displayObject.y;

    return displayObject.parent.localToGlobal(result);
  }

  public function getDisplayObject(o:Object):DisplayObject {
    return DisplayObject(o);
  }
}
