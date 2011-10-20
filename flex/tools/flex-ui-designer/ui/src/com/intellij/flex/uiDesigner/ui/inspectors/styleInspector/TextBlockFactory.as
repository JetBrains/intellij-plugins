package com.intellij.flex.uiDesigner.ui.inspectors.styleInspector {
import flash.errors.IllegalOperationError;
import flash.text.engine.TextBlock;

import org.tinytlf.ITextEngine;
import org.tinytlf.conversion.IContentElementFactory;
import org.tinytlf.conversion.ITextBlockGenerator;

[Abstract]
public class TextBlockFactory {
  protected var dataChanged:Boolean;
  
  protected var _engine:ITextEngine;
  public function get engine():ITextEngine {
    return _engine;
  }
  public function set engine(value:ITextEngine):void {
    _engine = value;
  }
  
  public function preRender():void {
    if (dataChanged) {
      dataChanged = false;
      _engine.analytics.clear();
    }
  }
  
  protected function markDataChanged(value:Object):void {
    if (_engine != null && value != null) {
      dataChanged = true;
      _engine.invalidate();
    }
  }
  
  public function getTextBlock(index:int):TextBlock {
    var block:TextBlock = _engine.analytics.getBlockAt(index);
    if (block != null) {
      return block;
    }
    
    return createBlock(index);
  }

  protected function createBlock(index:int):TextBlock {
    throw new IllegalOperationError("abstract");
  }
  
  public function hasElementFactory(element:*):Boolean {
    throw new IllegalOperationError();
  }

  public function getElementFactory(element:*):IContentElementFactory {
    throw new IllegalOperationError();
  }

  public function mapElementFactory(element:*, factoryClassOrFactory:Object):void {
    throw new IllegalOperationError();
  }

  public function unMapElementFactory(element:*):Boolean {
    throw new IllegalOperationError();
  }

  public function get textBlockGenerator():ITextBlockGenerator {
    throw new IllegalOperationError();
  }

  public function set textBlockGenerator(value:ITextBlockGenerator):void {
    throw new IllegalOperationError();
  }
}
}
