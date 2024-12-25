// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.config;

import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class KarmaConfigReferenceContributor extends PsiReferenceContributor {

  private static final String FILES_VAR_NAME = "files";
  private static class Holder {
    private static final ElementPattern<JSLiteralExpression> STRING_LITERAL_INSIDE_KARMA_CONFIG_FILE =
      PlatformPatterns.psiElement(JSLiteralExpression.class)
        .and(new FilterPattern(new ElementFilter() {
          @Override
          public boolean isAcceptable(Object element, PsiElement context) {
            PsiFile psiFile = context.getContainingFile();
            return KarmaUtil.isKarmaConfigFile(psiFile.getName(), false);
          }

          @Override
          public boolean isClassAcceptable(Class hintClass) {
            return true;
          }
        }));
  }

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(Holder.STRING_LITERAL_INSIDE_KARMA_CONFIG_FILE, new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(final @NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        JSLiteralExpression literalExpression = ObjectUtils.tryCast(psiElement, JSLiteralExpression.class);
        if (literalExpression == null) {
          return PsiReference.EMPTY_ARRAY;
        }
        if (KarmaBasePathFinder.isBasePathStringLiteral(literalExpression)) {
          return new BasePathFileReferenceSet(literalExpression, this).getAllReferences();
        }
        if (isFileStringLiteral(literalExpression)) {
          return new FilesFileReferenceSet(literalExpression, this).getAllReferences();
        }
        return PsiReference.EMPTY_ARRAY;
      }
    });
  }

  private static boolean isFileStringLiteral(@NotNull JSLiteralExpression literalExpression) {
    if (literalExpression.isQuotedLiteral()) {
      JSArrayLiteralExpression arrayLiteralExpression = ObjectUtils.tryCast(literalExpression.getParent(), JSArrayLiteralExpression.class);
      if (arrayLiteralExpression != null) {
        JSProperty property = ObjectUtils.tryCast(arrayLiteralExpression.getParent(), JSProperty.class);
        if (property != null) {
          String name = JsPsiUtils.getPropertyName(property);
          return FILES_VAR_NAME.equals(name);
        }
      }
    }
    return false;
  }

  private static class BasePathFileReferenceSet extends FileReferenceSet {
    BasePathFileReferenceSet(@NotNull JSLiteralExpression literalExpression, @NotNull PsiReferenceProvider psiReferenceProvider) {
      super(getString(literalExpression),
            literalExpression,
            1,
            psiReferenceProvider,
            !SystemInfo.isWindows,
            false,
            null,
            false);
      setEmptyPathAllowed(true);
      super.reparse();
    }

    private static String getString(@NotNull JSLiteralExpression expression) {
      // empty string resolves to a configuration file, but its parent directory is needed
      String value = StringUtil.unquoteString(expression.getText());
      return value.isEmpty() ? "." : value;
    }

    @Override
    public @NotNull Collection<PsiFileSystemItem> computeDefaultContexts() {
      if (isAbsolutePathReference()) {
        return toFileSystemItems(ManagingFS.getInstance().getLocalRoots());
      }

      return super.computeDefaultContexts();
    }

    @Override
    protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
      return FileReferenceSet.DIRECTORY_FILTER;
    }

    @Override
    public FileReference createFileReference(TextRange range, int index, String text) {
      return new KarmaConfigFileReference(this, range, index, text, KarmaConfigFileReference.FileType.DIRECTORY, false);
    }

    @Override
    protected boolean isSoft() {
      return true;
    }
  }

  private static class FilesFileReferenceSet extends FileReferenceSet {
    private boolean myPatternUsed = false;

    FilesFileReferenceSet(@NotNull JSLiteralExpression literalExpression, @NotNull PsiReferenceProvider psiReferenceProvider) {
      super(StringUtil.unquoteString(literalExpression.getText()),
            literalExpression,
            1,
            psiReferenceProvider,
            !SystemInfo.isWindows,
            false,
            null,
            false);
      setEmptyPathAllowed(true);
      super.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, file -> {
        if (!(file instanceof JSFile)) {
          return null;
        }
        PsiDirectory psiDirectory = file.getParent();
        if (psiDirectory == null) {
          return null;
        }
        String basePath = KarmaBasePathFinder.getInstance().fetchBasePath((JSFile) file);
        if (StringUtil.isEmpty(basePath)) {
          return Collections.singletonList(psiDirectory);
        }
        VirtualFile vDirectory = psiDirectory.getVirtualFile();
        VirtualFile vChildDirectory = vDirectory.findFileByRelativePath(basePath);
        if (vChildDirectory != null) {
          PsiDirectory psiChildDirectory = psiDirectory.getManager().findDirectory(vChildDirectory);
          if (psiChildDirectory != null) {
            return Collections.singletonList(psiChildDirectory);
          }
        }
        return Collections.emptyList();
      });
      super.reparse();
    }

    @Override
    public @NotNull Collection<PsiFileSystemItem> computeDefaultContexts() {
      if (isAbsolutePathReference()) {
        return toFileSystemItems(ManagingFS.getInstance().getLocalRoots());
      }

      return super.computeDefaultContexts();
    }

    @Override
    public FileReference createFileReference(TextRange range, int index, String text) {
      if (!myPatternUsed && text.contains("*")) {
        myPatternUsed = true;
      }
      return new KarmaConfigFileReference(this, range, index, text, KarmaConfigFileReference.FileType.FILE, myPatternUsed);
    }

    @Override
    protected boolean isSoft() {
      return true;
    }
  }

}
