package com.intellij.flex.uiDesigner {
import flash.net.Socket;

import org.jetbrains.actionSystem.DataContext;

public interface TestCase {
  function init(dataContext:DataContext, socket:Socket):void;
  
  function setUp():void;
  
  function set asyncSuccessHandler(value:Function):void;
}
}
