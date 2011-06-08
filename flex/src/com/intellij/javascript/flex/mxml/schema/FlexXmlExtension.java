package com.intellij.javascript.flex.mxml.schema;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.validation.fixes.CreateFieldByMxmlAttributeFix;
import com.intellij.lang.javascript.validation.fixes.CreateSetterByMxmlAttributeFix;
import com.intellij.lang.javascript.validation.fixes.FixAndIntentionAction;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.xml.DefaultXmlExtension;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;

public class FlexXmlExtension extends DefaultXmlExtension {
  public boolean isAvailable(final PsiFile file) {
    return JavaScriptSupportLoader.isFlexMxmFile(file);
  }

  public TagNameReference createTagNameReference(final ASTNode nameElement, final boolean startTagFlag) {
    return new MxmlTagNameReference(nameElement, startTagFlag);
  }

  public void createAddAttributeFix(@NotNull final XmlAttribute attribute, final HighlightInfo highlightInfo) {
    final String name = attribute.getName();
    if (!JSRefactoringUtil.isValidIdentifier(name, attribute.getProject())) {
      return;
    }

    final XmlElementDescriptor descriptor = attribute.getParent().getDescriptor();
    final PsiElement declaration = descriptor instanceof ClassBackedElementDescriptor ? descriptor.getDeclaration() : null;
    final VirtualFile virtualFile = declaration == null ? null : declaration.getContainingFile().getVirtualFile();
    if (virtualFile == null ||
        ProjectRootManager.getInstance(declaration.getProject()).getFileIndex().getSourceRootForFile(virtualFile) == null) {
      return;
    }

    // todo declaration can be XmlFile (other MXML)
    if (declaration instanceof JSClass) {
      final String attributeValue = attribute.getValue();

      final FixAndIntentionAction fix1 = new CreateFieldByMxmlAttributeFix(name, attributeValue);
      fix1.registerElementRefForFix(attribute, null);

      final FixAndIntentionAction fix2 = new CreateSetterByMxmlAttributeFix(name, attributeValue);
      fix2.registerElementRefForFix(attribute, null);

      QuickFixAction.registerQuickFixAction(highlightInfo, fix1);
      QuickFixAction.registerQuickFixAction(highlightInfo, fix2);
    }
  }
}
