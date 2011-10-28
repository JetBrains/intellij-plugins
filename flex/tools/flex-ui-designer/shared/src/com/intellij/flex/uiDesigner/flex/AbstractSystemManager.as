package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.UiErrorHandler;

import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.geom.Rectangle;
import flash.system.ApplicationDomain;

[Abstract]
public class AbstractSystemManager extends Sprite {
  protected var _document:DisplayObject;
  public function get document():Object {
    return _document;
  }

  // requires only for error reporting
  protected var _documentFactory:Object;
  public function get documentFactory():Object {
    return _documentFactory;
  }

  public function init(moduleFactory:Object, uiErrorHandler:UiErrorHandler,
                         mainFocusManager:MainFocusManagerSB, documentFactory:Object):void {
    _documentFactory = documentFactory;
  }

  protected const _explicitDocumentSize:Rectangle = new Rectangle();
  public function get explicitDocumentSize():Rectangle {
    return _explicitDocumentSize;
  }

  public function setActualDocumentSize(w:Number, h:Number):void {
    // originally set by setLayoutBoundsSize, but the Application without explicit size hangs on Stage and listen to resize - we can not change this behavior without the injection of the byte-code
    _document.width = w;
    _document.height = h;
  }

  public function addRealEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
    super.addEventListener(type, listener, useCapture);
  }

  public function removeRealEventListener(type:String, listener:Function):void {
    super.removeEventListener(type, listener);
  }
}
}
