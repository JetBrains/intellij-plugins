package com.intellij.flex.uiDesigner.ui.styleInspector {
import com.intellij.flex.uiDesigner.Module;
import com.intellij.flex.uiDesigner.Server;
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.ui.CssElementFormat;
import com.intellij.flex.uiDesigner.ui.ElementManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Shape;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.text.engine.ElementFormat;
import flash.text.engine.FontMetrics;
import flash.text.engine.TextElement;
import flash.text.engine.TextLine;
import flash.ui.Mouse;
import flash.ui.MouseCursor;
import flash.utils.getQualifiedClassName;
import flash.utils.getQualifiedSuperclassName;

import org.tinytlf.ITextEngine;
import org.tinytlf.layout.TinytlfSprite;
import org.tinytlf.layout.properties.LayoutProperties;
import org.tinytlf.util.fte.TextLineUtil;

public class Interactor {
  private static const NORMAL:int = 0;
  private static const ACTIVE:int = 1;
  
  private static var fakeInteractor:FakeInteractor;
  private static const sharedPoint:Point = new Point();
  
  private var server:Server;
  
  private var element:TextElement;
  private var engine:ITextEngine;
  private var state:int;
  private var prevElementFormat:ElementFormat;
  private var elementManager:ElementManager;
  
  private var outUpHandlerAdded:Boolean;
  private var mouseDownOnElement:Boolean;

  public function Interactor(elementManager:ElementManager, server:Server) {
    this.elementManager = elementManager;
    this.server = server;
  }
  
  private var _module:Module;
  public function set module(value:Module):void {
    _module = value;
  }

  //noinspection JSUnusedGlobalSymbols
  public function set pane(value:StylePane):void {
    value.skin.addEventListener(MouseEvent.MOUSE_UP, mouseUpHandler);
    value.skin.addEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
    value.skin.addEventListener(MouseEvent.MOUSE_MOVE, mouseMoveHandler);
    value.skin.addEventListener(MouseEvent.ROLL_OUT, mouseRollOutHandler);
  }

  public function configureTextEngine(textEngine:FlexTextEngine):void {
    if (fakeInteractor == null) {
      fakeInteractor = new FakeInteractor();
    }
    
    textEngine.interactor = fakeInteractor;
  }

  private function mouseRollOutHandler(event:MouseEvent):void {
    if (state == ACTIVE && !outUpHandlerAdded) {
      updateState(NORMAL);
    }
  }
  
  private function mouseMoveHandler(event:MouseEvent):void {
    if (outUpHandlerAdded) {
      return;
    }
    
    sharedPoint.x = event.localX;
    sharedPoint.y = event.localY;
    var line:TextLine;
    var container:TinytlfSprite = event.target as TinytlfSprite;
    if (container != null) {
      var objects:Array = container.getObjectsUnderPoint(sharedPoint);
      for (var i:int = 0, n:int = objects.length; i < n; i++) {
        if ((line = objects[i] as TextLine) != null) {
          break;
        }
      }
    }
    
    var newElement:TextElement;
    if (line != null) {
      var atomIndex:int = line.getAtomIndexAtPoint(event.stageX, event.stageY);
      newElement = atomIndex == -1 ? null : TextLineUtil.getElementAtAtomIndex(line, atomIndex) as TextElement; // may be GroupElement, i.e. outside of text content
    }
    
    if (newElement == element) {
      if (outUpHandlerAdded) {
        container.stage.removeEventListener(MouseEvent.MOUSE_UP, outUp);
        outUpHandlerAdded = false;
      }
      return;
    }
    else if (element != null) {
      if (state == ACTIVE && event.buttonDown) {
        outUpHandlerAdded = true;
        DisplayObject(event.target).stage.addEventListener(MouseEvent.MOUSE_UP, outUp);
        return;
      }
      else {
        updateState(NORMAL);
      }
    }
    
    if (newElement != null && newElement.userData != null) {
      element = newElement;
      engine = container.engine;
      if (event.buttonDown) {
        return; // over when select text
      }

      updateState(ACTIVE);
    }
    
    event.updateAfterEvent();
  }

  private function mouseUpHandler(event:MouseEvent):void {
    if (element == null || outUpHandlerAdded || !mouseDownOnElement) {
      return;
    }

    mouseDownOnElement = false;
    
    if (element.userData is String) {
      server.goToClass(_module, element.userData);
    }
    else if (LayoutProperties(element.textBlock.userData).constraint == null) {
      propertyReferenceClickHandler(element.userData);
    }
    else {
      fileSourceClickHandler(element.userData);
    }
    
    updateState(NORMAL);
    
    event.updateAfterEvent();
  }

  private function mouseDownHandler(event:MouseEvent):void {
    if (element != null) {
      mouseDownOnElement = true;
    }
  }
  
  private function fileSourceClickHandler(ruleset:CssRuleset):void {
    server.openFile(_module, ruleset.file.url, ruleset.textOffset);
  }
  
  private function propertyReferenceClickHandler(ruleset:CssRuleset):void {
     if (ruleset.textOffset == -1) {
      // case: ButtonBarSkin, middleButton element
      var uiComponent:Object = elementManager.element;
      var documentFQN:String = getQualifiedClassName(uiComponent.document).replace("::", ".");

      var elementFqn:String = getQualifiedClassName(uiComponent);
      if ("outerDocument" in uiComponent && elementFqn.lastIndexOf("InnerClass") != -1) {
        elementFqn = getQualifiedSuperclassName(uiComponent);
      }
      elementFqn = elementFqn.replace("::", ".");
      
      server.resolveExternalInlineStyleDeclarationSource(_module, documentFQN,  elementFqn, element.text, ruleset.declarations);
    }
    else {
       var name:String = element.text;
       var declaration:CssDeclaration = CssDeclaration(ruleset.declarationMap[name]);
       if (declaration == null && name == "skinClass") {
         declaration = CssDeclaration(ruleset.declarationMap.skinFactory);
       }
       server.openFile(_module, ruleset.file.url, declaration.textOffset);
    }
  }
  
  private function outUp(event:MouseEvent):void {
    event.currentTarget.removeEventListener(event.type, outUp, true);
    if (state == ACTIVE) {
      updateState(NORMAL);
    }
    
    outUpHandlerAdded = false;
  }

  private function updateState(newState:int):void {
//    trace(element.text + " " + (newState == 0 ? "normal" : "active"));
    if (state == newState) {
      return;
    }

    var container:DisplayObjectContainer = element.textBlock.firstLine.parent;
    if (state == ACTIVE) {
      Shape(container.getChildAt(0)).graphics.clear();
    }

    state = newState;

    if (newState == ACTIVE) {
      Mouse.cursor = MouseCursor.BUTTON;
      
      var g:Graphics = (container.getChildAt(0) as Shape || Shape(container.addChildAt(new Shape(), 0))).graphics;
      if (element.userData is String) {
        prevElementFormat = element.elementFormat;
        element.elementFormat = CssElementFormat.linkHover;
        engine.layout.renderInvalidBlock(element.textBlock, engine.layout.containers[0]);
      }
      drawUnderline(element.elementFormat, g);
    }
    else {
      Mouse.cursor = MouseCursor.AUTO;
      
      if (element.userData is String) {
        element.elementFormat = prevElementFormat;
        engine.layout.renderInvalidBlock(element.textBlock, engine.layout.containers[0]);
      }

      element = null;
      engine = null;
    }
  }
  
  private function drawUnderline(format:ElementFormat, g:Graphics):void {
    var fontMetrics:FontMetrics = format.getFontMetrics();
    g.lineStyle(fontMetrics.underlineThickness, format.color);

    var startIndex:int = element.textBlockBeginIndex;
    const endIndex:int = startIndex + element.rawText.length;
    var line:TextLine = element.textBlock.getTextLineAtCharIndex(startIndex);
    var xEqualsLineX:Boolean = line.textBlockBeginIndex == startIndex;
    var underlineOffset:Number = Math.ceil(fontMetrics.underlineOffset);
    while (true) {
      const y:Number = line.y + underlineOffset;
      g.moveTo(xEqualsLineX ? line.x : (line.x + line.getAtomBounds(line.getAtomIndexAtCharIndex(startIndex)).x), y);
      var lineEnd:int = line.textBlockBeginIndex + line.rawTextLength;
      if (lineEnd > endIndex) {
        g.lineTo(line.x + line.getAtomBounds(line.getAtomIndexAtCharIndex(endIndex)).x, y);
        break;
      }
      else {
        g.lineTo(line.x + line.width, y);
        if (lineEnd == endIndex) {
          break;
        }
        else {
          line = line.nextLine;
          xEqualsLineX = true;
        }
      }
    }
  }
}
}

import flash.events.Event;
import flash.events.EventDispatcher;

import org.tinytlf.ITextEngine;
import org.tinytlf.interaction.ITextInteractor;

final class FakeInteractor implements ITextInteractor {
  public function get engine():ITextEngine {
    return null;
  }

  public function set engine(textEngine:ITextEngine):void {
  }

  public function mapMirror(element:*, mirrorClassOrFactory:Object):void {
  }

  public function unMapMirror(element:*):Boolean {
    return false;
  }

  public function hasMirror(element:*):Boolean {
    return false;
  }

  public function getMirror(element:* = null):EventDispatcher {
    return element;
  }

  public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void {
  }

  public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
  }

  public function dispatchEvent(event:Event):Boolean {
    return false;
  }

  public function hasEventListener(type:String):Boolean {
    return false;
  }

  public function willTrigger(type:String):Boolean {
    return false;
  }
}
