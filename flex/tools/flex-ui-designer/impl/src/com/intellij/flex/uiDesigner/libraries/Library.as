package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.VirtualFileImpl;
import com.intellij.flex.uiDesigner.css.Stylesheet;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.Dictionary;
import flash.utils.IDataInput;

public class Library {
  private var _file:VirtualFile;
  public function get file():VirtualFile {
    return _file;
  }

  private var _inheritingStyles:Dictionary;
  public function get inheritingStyles():Dictionary {
    return _inheritingStyles;
  }

  private var _defaultsStyle:Stylesheet;
  public function get defaultsStyle():Stylesheet {
    return _defaultsStyle;
  }

  public function readExternal(input:IDataInput):void {
    _file = VirtualFileImpl.create(input);

    var n:int = input.readUnsignedShort();
    if (n > 0) {
      var stringTable:Vector.<String> = StringRegistry.instance.getTable();
      _inheritingStyles = new Dictionary();
      while (n-- > 0) {
        _inheritingStyles[stringTable[AmfUtil.readUInt29(input)]] = true;
      }
    }  

    if (input.readBoolean()) {
      _defaultsStyle = new Stylesheet();
      _defaultsStyle.read(input);
    }
  }
}
}