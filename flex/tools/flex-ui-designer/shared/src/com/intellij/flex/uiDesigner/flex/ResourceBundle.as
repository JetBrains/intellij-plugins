package com.intellij.flex.uiDesigner.flex {
import flash.utils.Dictionary;

public class ResourceBundle {
  private var _bundleName:String;
    private var _content:Dictionary;
    private var _locale:String;

    public function ResourceBundle(locale:String, bundleName:String, content:Dictionary) {
      _locale = locale;
      _bundleName = bundleName;
      _content = content;
    }

    public function get bundleName():String {
      return _bundleName;
    }

    public function get content():Object {
      return _content;
    }

    public function get locale():String {
      return _locale;
    }
}
}
