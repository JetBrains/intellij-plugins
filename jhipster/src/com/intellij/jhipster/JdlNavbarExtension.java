// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension;
import com.intellij.jhipster.psi.*;
import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.jhipster.JdlConstants.CONFIG_BLOCK_NAME;
import static com.intellij.jhipster.JdlConstants.DEPLOYMENT_BLOCK_NAME;

final class JdlNavbarExtension extends StructureAwareNavBarModelExtension {
  @Override
  protected @NotNull Language getLanguage() {
    return JdlLanguage.INSTANCE;
  }

  @Override
  public @Nullable Icon getIcon(Object object) {
    if (object instanceof JdlEntity) {
      return JdlIconsMapping.getEntityIcon();
    }
    if (object instanceof JdlEnum) {
      return JdlIconsMapping.getEnumIcon();
    }
    if (object instanceof JdlApplication) {
      return JdlIconsMapping.getApplicationIcon();
    }
    if (object instanceof JdlEntityFieldMapping) {
      return JdlIconsMapping.getFieldIcon();
    }
    if (object instanceof JdlEnumValue) {
      return JdlIconsMapping.getFieldIcon();
    }
    if (object instanceof JdlOptionNameValue) {
      return JdlIconsMapping.getPropertyIcon();
    }
    if (object instanceof JdlConfigBlock) {
      return JdlIconsMapping.getConfigIcon();
    }
    if (object instanceof JdlConfigurationOption) {
      return JdlIconsMapping.getConfigurationPropertyIcon();
    }
    if (object instanceof JdlRelationshipGroup) {
      return JdlIconsMapping.getRelationshipIcon();
    }
    if (object instanceof JdlDeployment) {
      return JdlIconsMapping.getDeployIcon();
    }
    return null;
  }

  @SuppressWarnings("HardCodedStringLiteral")
  @Override
  public @Nullable String getPresentableText(Object object) {
    if (object instanceof JdlEntity) {
      return ((JdlEntity)object).getName();
    }
    if (object instanceof JdlEnum) {
      return ((JdlEnum)object).getName();
    }
    if (object instanceof JdlApplication) {
      return ((JdlApplication)object).getName();
    }
    if (object instanceof JdlEntityFieldMapping) {
      return ((JdlEntityFieldMapping)object).getName();
    }
    if (object instanceof JdlEnumValue) {
      return ((JdlEnumValue)object).getName();
    }
    if (object instanceof JdlConfigBlock) {
      return CONFIG_BLOCK_NAME;
    }
    if (object instanceof JdlOptionNameValue) {
      return ((JdlOptionNameValue)object).getName();
    }
    if (object instanceof JdlConfigurationOption) {
      return ((JdlConfigurationOption)object).getName();
    }
    if (object instanceof JdlRelationshipGroup) {
      return ((JdlRelationshipGroup)object).getType();
    }
    if (object instanceof JdlDeployment) {
      return DEPLOYMENT_BLOCK_NAME;
    }
    return null;
  }
}
