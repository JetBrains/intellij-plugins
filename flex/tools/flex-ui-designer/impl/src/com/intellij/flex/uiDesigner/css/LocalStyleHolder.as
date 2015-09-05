package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.DocumentFactory;
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.VirtualFileImpl;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.ByteArray;
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public class LocalStyleHolder implements IExternalizable {
  private var _data:ByteArray;
  public function set data(value:ByteArray):void {
    _data = value;
    _stylesheet = null;
  }

  private var _file:VirtualFileImpl;
  public function get file():VirtualFile {
    return _file;
  }

  private var _users:Vector.<VirtualFile>;
  public function get users():Vector.<VirtualFile> {
    return _users;
  }

  private var _stylesheet:Stylesheet;
  public function get stylesheet():Stylesheet {
    if (_stylesheet == null) {
      _stylesheet = new Stylesheet();
      _stylesheet.read(_data);
      _file.stylesheet = _stylesheet;
      _data = null;
    }
    return _stylesheet;
  }

  public function isApplicable(documentFactory:DocumentFactory):Boolean {
    return _file == documentFactory.file || (_users != null && _users.indexOf(documentFactory.file) != -1);
  }

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    _file = VirtualFileImpl(VirtualFileImpl.create(input));
    _data = AmfUtil.readByteArray(input);
    const usersLength:int = input.readUnsignedByte();
    if (usersLength > 0) {
      _users = new Vector.<VirtualFile>(usersLength, true);
      for (var i:int = 0; i < usersLength; i++) {
        _users[i] = VirtualFileImpl.create(input);
      }
    }
  }
}
}