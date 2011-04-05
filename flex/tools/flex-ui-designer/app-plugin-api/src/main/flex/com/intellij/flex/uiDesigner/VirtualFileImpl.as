package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.Stylesheet;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.filesystem.File;
import flash.utils.Dictionary;
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public final class VirtualFileImpl implements IExternalizable, VirtualFile {
  private static const map:Dictionary = new Dictionary();
  
  private var _url:String;
  private var _presentableUrl:String;

  public function get url():String {
    return _url;
  }

  public function get presentableUrl():String {
    return _presentableUrl;
  }

  public var stylesheet:Stylesheet;
  
  private var _name:String;
  public function get name():String {
    if (_name == null) {
      var index:int = url.lastIndexOf("/");
      if (index < 0) {
        _name = url;
      }
      else {
        _name = url.substring(index + 1);
      }
    }

    return _name;
  }

  public function createChild(name:String):VirtualFile {
    return create2(url.charCodeAt(url.length - 1) == 47 ? (url + name) : (url + "/" + name), presentableUrl + File.separator + name);
  }

  public static function create(input:IDataInput):VirtualFile {
    return create2(input.readUTFBytes(AmfUtil.readUInt29(input)), input.readUTFBytes(AmfUtil.readUInt29(input)));
  }
  
  public static function create2(url:String, presentableUrl:String):VirtualFile {
    var file:VirtualFileImpl = map[url];
    if (file == null) {
      file = new VirtualFileImpl();
      file._url = url;
      file._presentableUrl = presentableUrl;
      map[url] = file;
    }
    
    return file;
  }

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    _url = input.readUTFBytes(AmfUtil.readUInt29(input));
    _presentableUrl = input.readUTFBytes(AmfUtil.readUInt29(input));
  }
}
}