package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.StringRegistry;

import flash.utils.Dictionary;
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public class Stylesheet implements IExternalizable {
  private var _namespaces:Dictionary;
  public function get namespaces():Dictionary {
    return _namespaces;
  }
  
  private var _rulesets:Vector.<CssRuleset>;
  public function get rulesets():Vector.<CssRuleset> {
    return _rulesets;
  }

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    _rulesets = input.readObject();
    
    const n:int = input.readUnsignedByte();
    if (n > 0) {
      var stringRegistry:StringRegistry = StringRegistry.instance;
      _namespaces = new Dictionary();
      while (n-- > 0) {
        _namespaces[stringRegistry.read(input)] = stringRegistry.read(input);
      }
    }
  }
}
}