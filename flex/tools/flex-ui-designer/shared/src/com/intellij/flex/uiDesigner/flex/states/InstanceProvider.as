package com.intellij.flex.uiDesigner.flex.states {
public interface InstanceProvider {
  function get nullableInstance():Object;
  function get bindingExecutor():DeferredInstanceFromBytesBase;
}
}
