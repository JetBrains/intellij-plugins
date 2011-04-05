package com.intellij.flex.uiDesigner.ui {
import com.intellij.flex.uiDesigner.*;

import cocoa.DataControl;

import com.intellij.flex.uiDesigner.flex.SystemManagerSB;

import flash.utils.getQualifiedClassName;

import org.flyti.plexus.Injectable;
import org.flyti.util.ArrayList;

public class ElementTreeBarManager implements Injectable {
  private const source:Vector.<Object> = new Vector.<Object>();
  private const sourceList:ArrayList = new ArrayList(source);

  public function set element(value:Object):void {
    if (_presentation == null) {
      return;
    }
    
    if (value == null) {
      _presentation.hidden = true;
      return;
    }

    var sourceItemCounter:int = 0;
    var element:Object = value;
    do {
      var qualifiedClassName:String = getQualifiedClassName(element);
      source[sourceItemCounter++] = qualifiedClassName.substr(qualifiedClassName.lastIndexOf("::") + 2);
    }
    while (!((element = element.parent) is SystemManagerSB));

    source.length = sourceItemCounter;

    if (_presentation.items == null) {
      _presentation.items = sourceList;
    }
    else {
      sourceList.refresh();
    }

    _presentation.hidden = false;
  }

  private var _presentation:DataControl;
  public function set presentation(value:DataControl):void {
    _presentation = value
  }
}
}