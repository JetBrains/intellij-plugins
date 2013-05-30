package com.intellij.javascript.karma.config;

import com.intellij.lang.javascript.psi.JSAssignmentExpression;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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

/**
 * @author Sergey Simonchik
 */
public class KarmaConfigReferenceContributor extends PsiReferenceContributor {

  private static final String FILE_NAME_SUFFIX = ".conf.js";
  private static final String BASE_PATH_NAME = "basePath";

  public static final ElementPattern<JSLiteralExpression> BASE_NAME_PLACE = PlatformPatterns.psiElement(JSLiteralExpression.class)
    .and(new FilterPattern(new ElementFilter() {
      public boolean isAcceptable(Object element, PsiElement context) {
        PsiFile psiFile = context.getContainingFile();
        return psiFile.getName().endsWith(FILE_NAME_SUFFIX);
      }

      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));

  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(BASE_NAME_PLACE, new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull final PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        JSLiteralExpression literalExpression = ObjectUtils.tryCast(psiElement, JSLiteralExpression.class);
        if (literalExpression == null) {
          return PsiReference.EMPTY_ARRAY;
        }
        JSAssignmentExpression assignmentExpression = ObjectUtils.tryCast(psiElement.getParent(), JSAssignmentExpression.class);
        if (assignmentExpression != null) {
          JSDefinitionExpression lOperand = ObjectUtils.tryCast(assignmentExpression.getLOperand(), JSDefinitionExpression.class);
          if (lOperand != null && BASE_PATH_NAME.equals(lOperand.getName())) {
            return new BasePathFileReferenceSet(literalExpression, this).getAllReferences();
          }
        }
        return PsiReference.EMPTY_ARRAY;
      }
    });
  }

  private static class BasePathFileReferenceSet extends FileReferenceSet {
    private final JSLiteralExpression myLiteralExpression;

    public BasePathFileReferenceSet(@NotNull JSLiteralExpression literalExpression,
                                    @NotNull PsiReferenceProvider psiReferenceProvider) {
      super(StringUtil.stripQuotesAroundValue(literalExpression.getText()),
            literalExpression,
            1,
            psiReferenceProvider,
            !SystemInfo.isWindows,
            false);
      myLiteralExpression = literalExpression;
      setEmptyPathAllowed(true);
    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> computeDefaultContexts() {
      if (isAbsolutePathReference()) {
        VirtualFile vFile = LocalFileSystem.getInstance().getRoot();
        final PsiDirectory directory = myLiteralExpression.getManager().findDirectory(vFile);
        if (directory != null) {
          return Collections.<PsiFileSystemItem>singleton(directory);
        }
      }
      return super.computeDefaultContexts();
    }

    @Override
    protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
      return FileReferenceSet.DIRECTORY_FILTER;
    }

    @Override
    public FileReference createFileReference(TextRange range, int index, String text) {
      return new KarmaConfigFileReference(this, range, index, text, KarmaConfigFileReference.FileType.DIRECTORY);
    }

    @Override
    protected boolean isSoft() {
      return true;
    }
  }

}
