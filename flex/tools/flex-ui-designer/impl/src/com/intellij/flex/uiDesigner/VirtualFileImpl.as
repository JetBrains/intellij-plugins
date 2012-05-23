package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.Stylesheet;
import com.intellij.flex.uiDesigner.io.AmfUtil;

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
      var index:int = _presentableUrl.lastIndexOf("/");
      if (index < 0) {
        index = _presentableUrl.lastIndexOf("\\");
      }
      _name = index < 0 ? _presentableUrl : _presentableUrl.substring(index + 1);
    }

    return _name;
  }

  public function createChild(name:String):VirtualFile {
    const newUrl:String = _url.charCodeAt(_url.length - 1) == 47 ? (_url + name) : (_url + "/" + name);
    var file:VirtualFileImpl = map[newUrl];
    return file == null ? createNew(newUrl, _presentableUrl + "/" + name) : file;
  }

  public static function create(input:IDataInput):VirtualFile {
    return create2(AmfUtil.readString(input), AmfUtil.readString(input));
  }
  
  private static function create2(url:String, presentableUrl:String):VirtualFile {
    var file:VirtualFileImpl = map[url];
    return file == null ? createNew(url, presentableUrl) : file;
  }

  private static function createNew(url:String, presentableUrl:String):VirtualFileImpl {
    return (map[url] = new VirtualFileImpl(url, presentableUrl));
  }
}
}