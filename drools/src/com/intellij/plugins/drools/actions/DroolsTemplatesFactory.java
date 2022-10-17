// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.actions;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.plugins.drools.DroolsBundle;
import com.intellij.plugins.drools.JbossDroolsIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DroolsTemplatesFactory implements FileTemplateGroupDescriptorFactory {
  @Override
  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final Icon icon = JbossDroolsIcons.Drools_16;
    final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(DroolsBundle.DROOLS_LIBRARY, icon);
    final FileTemplateDescriptor descriptor = new FileTemplateDescriptor("drools.rule.drl", icon) {
      @Override
      public @NotNull String getDisplayName() {
        return DroolsBundle.message("drools.rule.template.title");
      }
    };
    group.addTemplate(descriptor);
    return group;
  }
}
