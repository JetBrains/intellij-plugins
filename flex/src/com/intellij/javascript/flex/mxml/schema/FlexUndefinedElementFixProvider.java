// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml.schema;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.validation.fixes.CreateEventMetadataByMxmlAttributeFix;
import com.intellij.lang.javascript.validation.fixes.CreateFieldByMxmlAttributeFix;
import com.intellij.lang.javascript.validation.fixes.CreateSetterByMxmlAttributeFix;
import com.intellij.lang.javascript.validation.fixes.FixAndIntentionAction;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlUndefinedElementFixProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class FlexUndefinedElementFixProvider extends XmlUndefinedElementFixProvider {
  @Override
  public IntentionAction @Nullable [] createFixes(@NotNull XmlAttribute attribute) {
    if (!JavaScriptSupportLoader.isFlexMxmFile(attribute.getContainingFile())) return null;

    final String name = attribute.getName();
    if (!JSRefactoringUtil.isValidIdentifier(name, attribute.getProject())) {
      return IntentionAction.EMPTY_ARRAY;
    }

    final XmlElementDescriptor descriptor = attribute.getParent().getDescriptor();
    final PsiElement declaration = descriptor instanceof ClassBackedElementDescriptor ? descriptor.getDeclaration() : null;
    final VirtualFile virtualFile = declaration == null ? null : declaration.getContainingFile().getVirtualFile();
    if (virtualFile == null ||
        ProjectRootManager.getInstance(declaration.getProject()).getFileIndex().getSourceRootForFile(virtualFile) == null) {
      return IntentionAction.EMPTY_ARRAY;
    }

    if (declaration instanceof JSClass || declaration instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((XmlFile)declaration)) {
      final String attributeValue = attribute.getValue();

      final FixAndIntentionAction fix1 = new CreateFieldByMxmlAttributeFix(name, attributeValue);
      fix1.registerElementRefForFix(attribute, null);

      final FixAndIntentionAction fix2 = new CreateSetterByMxmlAttributeFix(name, attributeValue);
      fix2.registerElementRefForFix(attribute, null);

      final FixAndIntentionAction fix3 = new CreateEventMetadataByMxmlAttributeFix(name);
      fix3.registerElementRefForFix(attribute, null);

      return new IntentionAction[] { fix1, fix2, fix3 };
    }
    return IntentionAction.EMPTY_ARRAY;
  }
}
