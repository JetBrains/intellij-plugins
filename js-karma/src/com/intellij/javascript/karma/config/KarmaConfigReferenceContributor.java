package com.intellij.javascript.karma.config;

import com.intellij.lang.javascript.psi.*;
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
import com.intellij.util.Function;
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
  private static final String FILES_VAR_NAME = "files";

  public static final ElementPattern<JSLiteralExpression> STRING_LITERAL_INSIDE_KARMA_CONFIG_FILE = PlatformPatterns.psiElement(JSLiteralExpression.class)
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
    registrar.registerReferenceProvider(STRING_LITERAL_INSIDE_KARMA_CONFIG_FILE, new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull final PsiElement psiElement, @NotNull ProcessingContext processingContext) {
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
        JSAssignmentExpression assignmentExpression = ObjectUtils.tryCast(arrayLiteralExpression.getParent(), JSAssignmentExpression.class);
        if (assignmentExpression != null) {
          JSDefinitionExpression lOperand = ObjectUtils.tryCast(assignmentExpression.getLOperand(), JSDefinitionExpression.class);
          return lOperand != null && FILES_VAR_NAME.equals(lOperand.getName());
        }
      }
    }
    return false;
  }

  private static class BasePathFileReferenceSet extends FileReferenceSet {

    private final JSLiteralExpression myLiteralExpression;

    public BasePathFileReferenceSet(@NotNull JSLiteralExpression literalExpression,
                                    @NotNull PsiReferenceProvider psiReferenceProvider) {
      super(getString(literalExpression),
            literalExpression,
            1,
            psiReferenceProvider,
            !SystemInfo.isWindows,
            false,
            null,
            false);
      myLiteralExpression = literalExpression;
      setEmptyPathAllowed(true);
      super.reparse();
    }

    private static String getString(@NotNull JSLiteralExpression expression) {
      // empty string resolves to a configuration file, but its parent directory is needed
      String value = StringUtil.stripQuotesAroundValue(expression.getText());
      return value.isEmpty() ? "." : value;
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
      return new KarmaConfigFileReference(this, range, index, text, KarmaConfigFileReference.FileType.DIRECTORY, false);
    }

    @Override
    protected boolean isSoft() {
      return true;
    }
  }

  private static class FilesFileReferenceSet extends FileReferenceSet {

    private final JSLiteralExpression myLiteralExpression;
    private boolean myPatternUsed = false;

    public FilesFileReferenceSet(@NotNull JSLiteralExpression literalExpression,
                                 @NotNull PsiReferenceProvider psiReferenceProvider) {
      super(StringUtil.stripQuotesAroundValue(literalExpression.getText()),
            literalExpression,
            1,
            psiReferenceProvider,
            !SystemInfo.isWindows,
            false,
            null,
            false);
      myLiteralExpression = literalExpression;
      setEmptyPathAllowed(true);
      super.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, new Function<PsiFile, Collection<PsiFileSystemItem>>() {
        @Override
        public Collection<PsiFileSystemItem> fun(PsiFile file) {
          if (!(file instanceof JSFile)) {
            return null;
          }
          PsiDirectory psiDirectory = file.getParent();
          if (psiDirectory == null) {
            return null;
          }
          String basePath = KarmaBasePathFinder.getInstance().fetchBasePath((JSFile) file);
          if (StringUtil.isEmpty(basePath)) {
            return Collections.<PsiFileSystemItem>singletonList(psiDirectory);
          }
          VirtualFile vDirectory = psiDirectory.getVirtualFile();
          VirtualFile vChildDirectory = vDirectory.findFileByRelativePath(basePath);
          if (vChildDirectory != null) {
            PsiDirectory psiChildDirectory = psiDirectory.getManager().findDirectory(vChildDirectory);
            if (psiChildDirectory != null) {
              return Collections.<PsiFileSystemItem>singletonList(psiChildDirectory);
            }
          }
          return Collections.emptyList();
        }
      });
      super.reparse();
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
