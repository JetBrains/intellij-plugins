// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.javascript.JSLanguageUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.validation.ActionScriptUnusedImportsHelper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlChildRole;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.util.XmlRefCountHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Maxim.Mossienko
 */
public class ECMAScriptImportOptimizer implements ImportOptimizer {

  private static final Logger LOG = Logger.getInstance(ECMAScriptImportOptimizer.class.getName());

  @Override
  public boolean supports(@NotNull PsiFile file) {
    return file.getLanguage() == FlexSupportLoader.ECMA_SCRIPT_L4 || FlexSupportLoader.isFlexMxmFile(file);
  }

  @Override
  public @NotNull Runnable processFile(final @NotNull PsiFile file) {
    VirtualFile vFile = file.getViewProvider().getVirtualFile();
    if (vFile instanceof VirtualFileWindow) vFile = ((VirtualFileWindow)vFile).getDelegate();
    if (file.isPhysical() && !ProjectRootManager.getInstance(file.getProject()).getFileIndex().isInSourceContent(vFile)) {
      return EmptyRunnable.INSTANCE;
    }

    return () -> {
      Collection<FormatFixer> formatters = executeNoFormat(file);
      for (FormatFixer formatter : formatters) {
        formatter.fixFormat();
      }
    };
  }

  /**
   * @return formatters to be applied
   */
  public static List<FormatFixer> executeNoFormat(PsiFile file) {
    if (file.isPhysical() && !CommonRefactoringUtil.checkReadOnlyStatus(file)) return Collections.emptyList();

    final ActionScriptUnusedImportsHelper.Results unusedImportsResults = ActionScriptUnusedImportsHelper.getUnusedImports(file);
    Project project = file.getProject();

    try {
      // TODO consider using a single FormatFixer instance with multiple ranges
      List<FormatFixer> formatters = new ArrayList<>();

      for (Computable<JSElement> holderWrapper : unusedImportsResults.importsByHolder.keySet()) {
        final JSElement holder = holderWrapper.compute();
        Pair<PsiElement, Boolean> defaultInsertionPlace = ImportUtils.getImportInsertionPlace(holder);

        final Collection<String> fqnsToImport = unusedImportsResults.importsByHolder.get(holderWrapper);
        String importBlock = ImportUtils.createImportBlock(file, fqnsToImport);
        PsiElement newImports = PsiFileFactory.getInstance(project)
          .createFileFromText(JSUtils.DUMMY_FILE_NAME_PREFIX + JSLanguageUtil.getDefaultExtension(FlexSupportLoader.ECMA_SCRIPT_L4), importBlock);

        PsiElement firstAdded;
        if (defaultInsertionPlace != null) {
          boolean before = defaultInsertionPlace.second;
          PsiElement insertionPlace = defaultInsertionPlace.first;

          PsiElement earlyImport = ImportUtils.findEarlyImport(before ? insertionPlace : insertionPlace.getNextSibling());
          if (earlyImport != null) {
            insertionPlace = earlyImport;
            before = true;
          }
          if (before) {
            firstAdded = insertionPlace.getParent().addRangeBefore(newImports.getFirstChild(), newImports.getLastChild(), insertionPlace);
          }
          else {
            firstAdded = insertionPlace.getParent().addRangeAfter(newImports.getFirstChild(), newImports.getLastChild(), insertionPlace);
          }
        }
        else {
          firstAdded = holder.addRange(newImports.getFirstChild(), newImports.getLastChild());
        }

        // TODO better way to find the last added?
        PsiElement lastAdded = firstAdded.getNextSibling();

        int count = fqnsToImport.size();
        if (count > 1) {
          while (true) {
            if (lastAdded instanceof JSImportStatement) {
              if (--count == 1) {
                break;
              }
            }
            lastAdded = lastAdded.getNextSibling();
          }
        }

        formatters.add(FormatFixer.create(firstAdded, lastAdded, FormatFixer.Mode.Reformat));
      }

      for (JSImportStatement statement : unusedImportsResults.getAllImports()) {
        // linebreaks fix formatters should be executed before reformatting ones
        formatters.add(0, FormatFixer.create(statement, FormatFixer.Mode.FollowingWhitespace));
        statement.delete();
      }

      for (JSReferenceExpression reference : unusedImportsResults.fqnsToReplaceWithShortName) {
        String name = StringUtil.getShortName(reference.getReferencedName());
        reference.replace(JSPsiElementFactory.createJSExpression(name, reference));
      }

      if (file instanceof XmlFile && FlexSupportLoader.isFlexMxmFile(file)) {
        formatters.addAll(deleteUnusedNamespaces((XmlFile)file));
      }
      else if (file instanceof JSFile) {
        final PsiElement context = file.getContext();
        final PsiFile containingFile = context == null ? null : context.getContainingFile();
        if (containingFile instanceof XmlFile && FlexSupportLoader.isFlexMxmFile(containingFile)) {
          formatters.addAll(deleteUnusedNamespaces((XmlFile)containingFile));
        }
      }

      return FormatFixer.mergeSingleFile(formatters);
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
      return Collections.emptyList();
    }
  }

  private static List<FormatFixer> deleteUnusedNamespaces(final XmlFile file) {
    final List<FormatFixer> formatters = new ArrayList<>();

    final XmlTag rootTag = file.getRootTag();
    if (rootTag != null) {
      for (XmlAttribute attribute : rootTag.getAttributes()) {
        if (!attribute.isNamespaceDeclaration()) continue;

        final String namespace = attribute.getValue();
        if (FlexSupportLoader.isLanguageNamespace(namespace)) continue;

        final XmlRefCountHolder refCountHolder = XmlRefCountHolder.getRefCountHolder(file);
        if (refCountHolder == null) continue;

        final String declaredPrefix = attribute.getName().contains(":") ? attribute.getLocalName() : "";
        if (namespace != null && !refCountHolder.isInUse(declaredPrefix)) {
          if (isFirstAttributeWithTagStartAtTheSameLine(attribute)) {
            JSRefactoringUtil.deleteSiblingWhitespace(attribute, false, true);
          }
          if (isLastAttributeWithTagEndAtTheSameLine(attribute)) {
            JSRefactoringUtil.deleteSiblingWhitespace(attribute, true, true);
          }

          attribute.delete();

          final ASTNode tagEndNode = XmlChildRole.START_TAG_END_FINDER.findChild(rootTag.getNode());
          if (tagEndNode != null) {
            formatters.add(FormatFixer.create(file, TextRange.create(rootTag.getTextRange().getStartOffset(),
                                                                     tagEndNode.getTextRange().getEndOffset()),
                                              FormatFixer.Mode.Reformat));
          }
          else {
            formatters.add(FormatFixer.create(rootTag, FormatFixer.Mode.Reformat));
          }
        }
      }
    }
    return formatters;
  }

  private static boolean isLastAttributeWithTagEndAtTheSameLine(final XmlAttribute attribute) {
    PsiElement sibling = attribute;
    while ((sibling = sibling.getNextSibling()) != null) {
      if (sibling instanceof XmlToken &&
          (((XmlToken)sibling).getTokenType() == XmlTokenType.XML_TAG_END ||
           ((XmlToken)sibling).getTokenType() == XmlTokenType.XML_EMPTY_ELEMENT_END)) {
        return true;
      }

      if (!(sibling instanceof PsiWhiteSpace)) return false;
      if (sibling.getText().indexOf('\n') >= 0) return false;
    }

    return false;
  }

  private static boolean isFirstAttributeWithTagStartAtTheSameLine(final XmlAttribute attribute) {
    PsiElement sibling = attribute;
    while ((sibling = sibling.getPrevSibling()) != null) {
      if (sibling instanceof XmlToken && ((XmlToken)sibling).getTokenType() == XmlTokenType.XML_NAME) {
        final PsiElement prevSibling = sibling.getPrevSibling();
        return prevSibling instanceof XmlToken && ((XmlToken)prevSibling).getTokenType() == XmlTokenType.XML_START_TAG_START;
      }

      if (!(sibling instanceof PsiWhiteSpace)) return false;
      if (sibling.getText().indexOf('\n') >= 0) return false;
    }

    return false;
  }
}
