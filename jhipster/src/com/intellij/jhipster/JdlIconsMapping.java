// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.icons.AllIcons;
import com.intellij.jhipster.icons.JhipsterIcons;

import javax.swing.Icon;

import static com.intellij.ui.LayeredIcon.layeredIcon;

public final class JdlIconsMapping {
  public static final Icon FILE_ICON = AllIcons.FileTypes.UiForm;
  private static final Icon REQUIRED_FIELD = layeredIcon(new Icon[]{AllIcons.Nodes.Field, JhipsterIcons.RequiredMark});

  private static final Icon APPLICATION_ICON = AllIcons.RunConfigurations.Application;
  private static final Icon CONFIG_ICON = AllIcons.FileTypes.Xml;
  private static final Icon ENTITY_ICON = AllIcons.Javaee.PersistenceEntity;
  private static final Icon DEPLOY_ICON = AllIcons.General.ProjectStructure;
  private static final Icon RELATION_ICON = AllIcons.Hierarchy.Subtypes;

  public static Icon getRelationshipIcon() {
    return RELATION_ICON;
  }

  public static Icon getEntityIcon() {
    return ENTITY_ICON;
  }

  public static Icon getEnumIcon() {
    return AllIcons.Nodes.Enum;
  }

  public static Icon getConfigurationPropertyIcon() {
    return AllIcons.Nodes.PropertyWriteStatic;
  }

  public static Icon getPropertyIcon() {
    return AllIcons.Nodes.PropertyWrite;
  }

  public static Icon getConfigIcon() {
    return CONFIG_ICON;
  }

  public static Icon getApplicationIcon() {
    return APPLICATION_ICON;
  }

  public static Icon getFieldIcon() {
    return AllIcons.Nodes.Field;
  }

  public static Icon getRequiredFieldIcon() {
    return REQUIRED_FIELD;
  }

  public static Icon getConstantIcon() {
    return AllIcons.Nodes.Constant;
  }

  public static Icon getDeployIcon() {
    return DEPLOY_ICON;
  }

  public static Icon getFieldConstraintIcon() {
    return AllIcons.General.InspectionsOK;
  }

  public static Icon getFieldTypeIcon() {
    return AllIcons.Nodes.Type;
  }
}
