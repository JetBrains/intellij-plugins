package com.intellij.flex.uiDesigner {
import flash.net.Socket;

public interface TestCase {
  function init(project:Project, socket:Socket):void;
  
  function setUp():void;
  
  function set asyncSuccessHandler(value:Function):void;
}
}
