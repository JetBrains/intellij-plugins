package com.google.jstestdriver.idea.testIntegration;

public class JstdQUnitCreateTeardownAction extends JstdQUnitCreateModuleLifecycleBaseAction {

  @Override
  protected String getModuleLifecyclePrecedingMethodName() {
    return JstdQUnitCreateModuleLifecycleBaseAction.MODULE_SETUP_METHOD_NAME;
  }

  @Override
  protected String getModuleLifecycleMethodName() {
    return JstdQUnitCreateModuleLifecycleBaseAction.MODULE_TEARDOWN_METHOD_NAME;
  }

  @Override
  protected String getActionDisplayName() {
    return "TearDown";
  }

}
