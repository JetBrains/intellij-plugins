package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.Stylesheet;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.filesystem.File;
import flash.utils.Dictionary;
import flash.utils.IDataInput;

public final class VirtualFileImpl implements VirtualFile {
  private static const map:Dictionary = new Dictionary();

  public var stylesheet:Stylesheet;

  public function VirtualFileImpl(url:String, presentableUrl:String) {
    _url = url;
    _presentableUrl = presentableUrl;
  }
  
  private var _url:String;
  public function get url():String {
    return _url;
  }

  private var _presentableUrl:String;
  public function get presentableUrl():String {
    return _presentableUrl;
  }
  
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
    const newUrl:String = url.charCodeAt(url.length - 1) == 47 ? (url + name) : (url + "/" + name);
    var file:VirtualFileImpl = map[newUrl];
    return file == null ? createNew(newUrl, presentableUrl + File.separator + name) : file;
  }

  public static function create(input:IDataInput):VirtualFile {
    return create2(AmfUtil.readString(input), AmfUtil.readString(input));
  }
  
  private static function create2(url:String, presentableUrl:String):VirtualFile {
    var file:VirtualFileImpl = map[url];
    return file == null ? createNew(url, presentableUrl) : file;
  }

  private static function createNew(url:String, presentableUrl:String):VirtualFileImpl {
    var file:VirtualFileImpl = new VirtualFileImpl(url, presentableUrl);
    map[url] = file;
    return file;
  }
}
}