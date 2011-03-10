package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.ProjectManager;

public interface TestCase {
  function setUp(projectManager:ProjectManager):void;
  
  function set asyncSuccessHandler(value:Function):void;
}
}
