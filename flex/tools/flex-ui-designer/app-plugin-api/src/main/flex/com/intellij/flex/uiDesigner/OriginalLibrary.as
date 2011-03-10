package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.Stylesheet;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.Dictionary;
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public class OriginalLibrary implements IExternalizable, Library {
  private var _path:String;
  public function get path():String {
    return _path;
  }
  
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

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    _path = input.readUTFBytes(AmfUtil.readUInt29(input));
    _file = VirtualFileImpl.create(input);

    _inheritingStyles = input.readObject();

    if (input.readBoolean()) {
      _defaultsStyle = new Stylesheet();
      _defaultsStyle.readExternal(input);
    }
  }
}
}