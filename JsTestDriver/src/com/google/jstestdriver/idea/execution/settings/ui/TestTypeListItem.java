package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.TestType;
import org.jetbrains.annotations.NotNull;

class TestTypeListItem implements IdProvider, RunSettingsSectionProvider {

  private final TestType myTestType;
  private final String myDisplayName;
  private final RunSettingsSection myRunSettingsSection;

  TestTypeListItem(@NotNull TestType testType, @NotNull String displayName, @NotNull RunSettingsSection runSettingsSection) {
    myTestType = testType;
    myDisplayName = displayName;
    myRunSettingsSection = runSettingsSection;
  }

  public TestType getTestType() {
    return myTestType;
  }

  @Override
  public RunSettingsSection provideRunSettingsSection() {
    return myRunSettingsSection;
  }

  @NotNull
  public String getDisplayName() {
    return myDisplayName;
  }

  @Override
  public String getId() {
    return myTestType.name();
  }

}
