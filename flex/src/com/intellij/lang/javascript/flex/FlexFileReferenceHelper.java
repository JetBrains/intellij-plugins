// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FlexFileReferenceHelper extends FileReferenceHelper {
  @Override
  public @NotNull Collection<PsiFileSystemItem> getContexts(final @NotNull Project project, final @NotNull VirtualFile file) {
    return Collections.emptyList();
  }

  @Override
  public boolean isMine(final @NotNull Project project, final @NotNull VirtualFile file) {
    return false;
  }

  @Override
  public @NotNull List<? extends @NotNull LocalQuickFix> registerFixes(final @NotNull FileReference reference) {
    final PsiElement element = reference.getElement();
    if (!(reference instanceof JSFlexFileReference) || !(element instanceof JSAttributeNameValuePair)) return Collections.emptyList();

    final PsiElement parent = element.getParent();
    if (!(parent instanceof JSAttribute) || !FlexAnnotationNames.EMBED.equals(((JSAttribute)parent).getName())) {
      return Collections.emptyList();
    }

    final String value = ((JSAttributeNameValuePair)element).getSimpleValue();
    if (value.startsWith("/")) return Collections.emptyList();

    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module == null) return Collections.emptyList();

    final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    final VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
    final boolean testSourceRoot = virtualFile != null && rootManager.getFileIndex().isInTestSourceContent(virtualFile);

    for (VirtualFile sourceRoot : rootManager.getSourceRoots(testSourceRoot)) {
      if (sourceRoot.findFileByRelativePath(value) != null) {
        return Collections.singletonList(new AddLeadingSlashFix((JSAttributeNameValuePair)element));
      }
    }

    return Collections.emptyList();
  }

  private static final class AddLeadingSlashFix extends LocalQuickFixAndIntentionActionOnPsiElement {
    private AddLeadingSlashFix(final JSAttributeNameValuePair element) {
      super(element);
    }

    @Override
    public @NotNull String getText() {
      return "Add leading slash";
    }

    @Override
    public @NotNull String getFamilyName() {
      return getText();
    }


    @Override
    public void invoke(@NotNull Project project,
                       @NotNull PsiFile file,
                       @Nullable Editor editor,
                       @NotNull PsiElement startElement,
                       @NotNull PsiElement endElement) {
      final ASTNode oldValueNode = ((JSAttributeNameValuePair)startElement).getValueNode();
      final String oldText = oldValueNode.getText();
      char quoteChar = !oldText.isEmpty() ? oldText.charAt(0) : '"';
      if (quoteChar != '\'' && quoteChar != '"') {
        quoteChar = '"';
      }

      final String newText = quoteChar + "/" + StringUtil.stripQuotesAroundValue(oldText) + quoteChar;
      final ASTNode newNode = JSChangeUtil.createExpressionFromText(project, newText);
      startElement.getNode().replaceChild(oldValueNode, newNode.getFirstChildNode());
    }
  }
}
