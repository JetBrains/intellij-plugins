package com.google.jstestdriver.idea.testIntegration;

public class JstdQUnitCreateSetupAction extends JstdQUnitCreateModuleLifecycleBaseAction {

  @Override
  protected String getModuleLifecyclePrecedingMethodName() {
    return null;
  }

  @Override
  protected String getModuleLifecycleMethodName() {
    return JstdQUnitCreateModuleLifecycleBaseAction.MODULE_SETUP_METHOD_NAME;
  }

  @Override
  protected String getActionDisplayName() {
    return "SetUp";
  }

}
