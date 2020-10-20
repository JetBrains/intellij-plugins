// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.bnd.imp.BndProjectImporter;

import javax.swing.*;

import static com.intellij.openapi.util.NotNullLazyValue.createValue;
import static org.osmorc.i18n.OsmorcBundle.message;

public final class BndRunConfigurationType extends ConfigurationTypeBase {
  private static final String ID = "osgi.bnd.run";

  @NotNull
  public static BndRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(BndRunConfigurationType.class);
  }

  public BndRunConfigurationType() {
    super(ID, message("bnd.configuration.name"), message("bnd.configuration.description"), createValue(() -> OsmorcIdeaIcons.Bnd));
    addFactory(new LaunchFactory(this));
    addFactory(new TestFactory(this));
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.osgi.bnd.run";
  }

  private static abstract class FactoryBase extends ConfigurationFactory {
    private final @Nls String myName;
    private final NotNullLazyValue<? extends Icon> myIcon;
    private final String myId;

    FactoryBase(ConfigurationType type, @Nls String name, String id, NotNullLazyValue<? extends Icon> icon) {
      super(type);
      myName = name;
      myIcon = icon;
      myId = id;
    }

    @Override
    public @Nls @NotNull String getName() {
      return myName;
    }

    @Override
    public @NotNull String getId() {
      return myId;
    }

    @Override
    public Icon getIcon() {
      return myIcon.getValue();
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
      return BndProjectImporter.getWorkspace(project) != null;
    }

    @Nullable
    @Override
    public Class<? extends BaseState> getOptionsClass() {
      return BndRunConfigurationOptions.class;
    }
  }

  private static class LaunchFactory extends FactoryBase {
    LaunchFactory(@NotNull ConfigurationType type) {
      super(type, message("bnd.run.configuration.name"), "Run Launcher", createValue(() -> OsmorcIdeaIcons.BndLaunch));
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new BndRunConfigurationBase.Launch(project, this, "");
    }
  }

  private static class TestFactory extends FactoryBase {
    TestFactory(@NotNull ConfigurationType type) {
      super(type, message("bnd.test.configuration.name"), "Test Launcher (JUnit)", createValue(() -> OsmorcIdeaIcons.BndTest));
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new BndRunConfigurationBase.Test(project, this, "");
    }
  }
}
