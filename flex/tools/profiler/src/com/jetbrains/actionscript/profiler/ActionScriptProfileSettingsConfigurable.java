// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.actionscript.profiler;

import com.intellij.openapi.options.SearchableConfigurable;
import com.jetbrains.actionscript.profiler.model.ActionScriptProfileSettings;
import com.jetbrains.actionscript.profiler.ui.ActionScriptProfileSettingsForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ActionScriptProfileSettingsConfigurable implements SearchableConfigurable {
  private final ActionScriptProfileSettings mySettings = ActionScriptProfileSettings.getInstance();
  private ActionScriptProfileSettingsForm mySettingsPane;

  @Override
  public String getDisplayName() {
    return ProfilerBundle.message("profile.settings.name");
  }

  @Override
  @NotNull
  public String getId() {
    return "asprofile.settings";
  }

  @Override
  public JComponent createComponent() {
    if (mySettingsPane == null) {
      mySettingsPane = new ActionScriptProfileSettingsForm();
    }
    reset();
    return mySettingsPane.getPanel();
  }

  @Override
  public boolean isModified() {
    return mySettingsPane != null && mySettingsPane.isModified(mySettings);
  }

  @Override
  public void apply() {
    if (mySettingsPane != null) {
      mySettingsPane.applyEditorTo(mySettings);
    }
  }

  @Override
  public void reset() {
    if (mySettingsPane != null) {
      mySettingsPane.resetEditorFrom(mySettings);
    }
  }

  @Override
  public void disposeUIResources() {
    mySettingsPane = null;
  }
}
