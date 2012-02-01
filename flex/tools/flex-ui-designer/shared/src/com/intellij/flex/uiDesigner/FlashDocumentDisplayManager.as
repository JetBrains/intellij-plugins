package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.designSurface.LayoutManager;
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;

import flash.display.DisplayObject;
import flash.display.Stage;
import flash.system.ApplicationDomain;

public class FlashDocumentDisplayManager extends AbstractDocumentDisplayManager implements DocumentDisplayManager {
  public function get componentInfoProvider():ComponentInfoProvider {
    return FlashComponentInfoProvider.instance;
  }

  public function get minDocumentWidth():int {
    return 0;
  }

  public function get minDocumentHeight():int {
    return 0;
  }

  public function get actualDocumentWidth():int {
    return _document.width;
  }

  public function get actualDocumentHeight():int {
    return _document.height;
  }

  public function get sharedInitialized():Boolean {
    return true;
  }

  public function getDefinitionByName(name:String):Object {
    return ApplicationDomain.currentDomain.getDefinition(name);
  }

  public function initShared(stageForAdobeDummies:Stage, resourceBundleProvider:ResourceBundleProvider, uiErrorHandler:UiErrorHandler):void {
  }

  override public function init(moduleFactory:Object, uiErrorHandler:UiErrorHandler,
                         mainFocusManager:MainFocusManagerSB, documentFactory:Object):void {
    super.init(moduleFactory, uiErrorHandler, mainFocusManager, documentFactory);
  }

  public function setDocument(object:DisplayObject):void {
    removeEventHandlers();

    if (_document != null) {
      removeChild(_document);
    }

    _document = object;

    _explicitDocumentWidth = initialExplicitDimension(object.width);
    _explicitDocumentHeight = initialExplicitDimension(object.height);

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

  public function get flexLayoutManager():Object {
    return null;
  }

  public function get layoutManager():LayoutManager {
    return null;
  }
}
}

import com.intellij.flex.uiDesigner.ComponentInfoProvider;
import com.intellij.flex.uiDesigner.DocumentDisplayManager;

import flash.display.DisplayObject;
import flash.display.Stage;
import flash.geom.Point;
import flash.utils.getQualifiedClassName;

final class FlashComponentInfoProvider implements ComponentInfoProvider {
  private static const sharedPoint:Point = new Point();

  private static var _instance:FlashComponentInfoProvider;
  internal static function get instance():ComponentInfoProvider {
    if (_instance == null) {
      _instance = new FlashComponentInfoProvider();
    }
    return _instance;
  }

  public function getComponentUnderPoint(stage:Stage, stageX:Number, stageY:Number):Object {
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
