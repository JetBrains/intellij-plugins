package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.AssetLoadSemaphore;
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.VirtualFileImpl;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.ByteArray;
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public class LocalStyleHolder implements IExternalizable {
  private var data:ByteArray;

  private var _file:VirtualFileImpl;
  public function get file():VirtualFile {
    return _file;
  }

  private var _stylesheet:Stylesheet;
  public function getStylesheet(assetLoadSemaphore:AssetLoadSemaphore):Stylesheet {
    if (_stylesheet == null) {
      _stylesheet = new Stylesheet();
      _stylesheet.readExternal(data, assetLoadSemaphore);
      _file.stylesheet = _stylesheet;
      data = null;
    }
    return _stylesheet;
  }

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    _file = VirtualFileImpl(VirtualFileImpl.create(input));

    data = new ByteArray();
    input.readBytes(data, 0, AmfUtil.readUInt29(input));
  }
}
}