// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.module;

import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.bc.impl.JpsFlexBuildConfigurationManagerImpl;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.module.JpsModuleType;
import org.jetbrains.jps.model.serialization.module.JpsModulePropertiesSerializer;

public final class JpsFlexModuleType extends JpsElementTypeBase<JpsFlexBuildConfigurationManager> implements JpsModuleType<JpsFlexBuildConfigurationManager> {
  public static final JpsFlexModuleType INSTANCE = new JpsFlexModuleType();
  private static final String ID = "Flex";

  private JpsFlexModuleType() {
  }

  public static JpsModulePropertiesSerializer<JpsFlexBuildConfigurationManager> createModulePropertiesSerializer() {
    return new JpsModulePropertiesSerializer<JpsFlexBuildConfigurationManager>(INSTANCE, ID, "FlexBuildConfigurationManager") {
      @Override
      public JpsFlexBuildConfigurationManager loadProperties(final @Nullable Element componentElement) {
        final JpsFlexBuildConfigurationManagerImpl manager = new JpsFlexBuildConfigurationManagerImpl();
        manager.loadState(XmlSerializer.deserialize(componentElement, JpsFlexBuildConfigurationManagerImpl.State.class));
        return manager;
      }
    };
  }
}
