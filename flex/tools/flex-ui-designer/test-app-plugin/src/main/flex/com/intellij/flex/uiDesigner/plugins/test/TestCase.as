package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.ProjectManager;

import flash.net.Socket;

public interface TestCase {
  function init(projectManager:ProjectManager, socket:Socket):void;
  
  function setUp():void;
  
  function set asyncSuccessHandler(value:Function):void;
}
}
