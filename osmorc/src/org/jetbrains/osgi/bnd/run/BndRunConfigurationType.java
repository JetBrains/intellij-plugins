// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.LazyUtil;
import icons.OsmorcIdeaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.bnd.imp.BndProjectImporter;

import javax.swing.*;

import static org.osmorc.i18n.OsmorcBundle.message;

public class BndRunConfigurationType extends ConfigurationTypeBase {
  private static final String ID = "osgi.bnd.run";

  @NotNull
  public static BndRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(BndRunConfigurationType.class);
  }

  public BndRunConfigurationType() {
    super(ID, message("bnd.configuration.name"), message("bnd.configuration.description"), LazyUtil.create(() -> OsmorcIdeaIcons.Bnd));
    addFactory(new LaunchFactory(this));
    addFactory(new TestFactory(this));
  }

  private static abstract class FactoryBase extends ConfigurationFactory {
    private final String myName;
    private final Icon myIcon;

    public FactoryBase(@NotNull ConfigurationType type, @NotNull String name, @NotNull Icon icon) {
      super(type);
      myName = name;
      myIcon = icon;
    }

    @Override
    public String getName() {
      return myName;
    }

    @Override
    public Icon getIcon() {
      return myIcon;
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
      return BndProjectImporter.getWorkspace(project) != null;
    }
  }

  private static class LaunchFactory extends FactoryBase {
    public LaunchFactory(@NotNull ConfigurationType type) {
      super(type, message("bnd.run.configuration.name"), OsmorcIdeaIcons.BndLaunch);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new BndRunConfigurationBase.Launch(project, this, "");
    }
  }

  private static class TestFactory extends FactoryBase {
    public TestFactory(@NotNull ConfigurationType type) {
      super(type, message("bnd.test.configuration.name"), OsmorcIdeaIcons.BndTest);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new BndRunConfigurationBase.Test(project, this, "");
    }
  }
}