package com.intellij.flex.uiDesigner.ui.styleInspector {
import com.intellij.flex.uiDesigner.ui.CssElementFormat;

import flash.text.engine.ContentElement;
import flash.text.engine.GroupElement;
import flash.text.engine.TextBlock;
import flash.text.engine.TextElement;

import org.tinytlf.conversion.ITextBlockFactory;
import org.tinytlf.util.fte.TextBlockUtil;

public class NamespaceListTextBlockFactory extends TextBlockFactory implements ITextBlockFactory {
  private var namespaceUnificator:NamespaceUnificator;
  
  public function get data():Object {
    return namespaceUnificator;
  }

  public function set data(value:Object):void {
    if (value == namespaceUnificator) {
      //return; 
    }
    
    namespaceUnificator = NamespaceUnificator(value);
    markDataChanged(value);
  }

  public function get numBlocks():int {
    return namespaceUnificator.uriList.length;
  }

  override protected function createBlock(index:int):TextBlock {
    if (index >= numBlocks) {
      return null;
    }
    
    var block:TextBlock = TextBlockUtil.checkOut();
    var uri:String = namespaceUnificator.uriList[index];
    var prefix:String = namespaceUnificator.getNamespacePrefix2(uri);
    var elements:Vector.<ContentElement> = new Vector.<ContentElement>(prefix == null ? 3 : 4, true);
    var i:int = 1;
    elements[0] = new TextElement("@namespace", CssElementFormat.identifier);
    if (prefix != null) {
      elements[i++] = new TextElement(" " + prefix, CssElementFormat.identifier);
    }
    elements[i++] = new TextElement(' "' + uri + '"', CssElementFormat.string);
    elements[i++] = new TextElement(";", CssElementFormat.defaultText);
    block.content = new GroupElement(elements);
    return block;
  }
}
}
