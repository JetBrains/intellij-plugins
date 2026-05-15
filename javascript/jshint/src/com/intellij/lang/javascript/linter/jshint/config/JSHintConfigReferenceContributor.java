package com.intellij.lang.javascript.linter.jshint.config;

import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class JSHintConfigReferenceContributor extends PsiReferenceContributor {
  private static class Holder {
    private static final ElementPattern<JsonStringLiteral> EXTENDS_VALUE_PLACE = PlatformPatterns.psiElement(JsonStringLiteral.class)
      .and(new FilterPattern(new ElementFilter() {
        @Override
        public boolean isAcceptable(Object element, PsiElement context) {
          JsonStringLiteral literalExpression = ObjectUtils.tryCast(element, JsonStringLiteral.class);
          return literalExpression != null && JSHintConfigFileUtil.isJSHintConfigFile(literalExpression);
        }

        @Override
        public boolean isClassAcceptable(Class hintClass) {
          return true;
        }
      }));
  }

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
    psiReferenceRegistrar.registerReferenceProvider(Holder.EXTENDS_VALUE_PLACE, new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(final @NotNull PsiElement psiElement,
                                                             @NotNull ProcessingContext processingContext) {
        JsonStringLiteral literalExpression = ObjectUtils.tryCast(psiElement, JsonStringLiteral.class);
        if (literalExpression != null) {
          return getReferencesByLiteralExpression(literalExpression, this);
        }
        return PsiReference.EMPTY_ARRAY;
      }
    });
  }

  private static PsiReference @NotNull [] getReferencesByLiteralExpression(@NotNull JsonStringLiteral literalExpression,
                                                                           @NotNull PsiReferenceProvider psiReferenceProvider) {
    JsonProperty property = JSLinterConfigFileUtil.getProperty(literalExpression);
    if (property == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    PsiElement keyElement = JSLinterConfigFileUtil.getFirstChildAsStringLiteral(property);
    if (keyElement != null && literalExpression == property.getValue() && JSHintConfigFileUtil.isExtendsKey(keyElement)) {
      String path = literalExpression.getValue();
      FileReferenceSet fileReferenceSet = new ExtendsFileReferenceSet(path, literalExpression, psiReferenceProvider);
      return fileReferenceSet.getAllReferences();
    }
    return PsiReference.EMPTY_ARRAY;
  }

  private static class ExtendsFileReferenceSet extends FileReferenceSet {

    ExtendsFileReferenceSet(@NotNull String path,
                            @NotNull JsonStringLiteral literalExpression,
                            @NotNull PsiReferenceProvider psiReferenceProvider) {
      super(path, literalExpression, 1, psiReferenceProvider, literalExpression.getContainingFile().getViewProvider().getVirtualFile().isCaseSensitive(), false);
    }

    @Override
    public @NotNull Collection<PsiFileSystemItem> computeDefaultContexts() {
      if (isAbsolutePathReference()) {
        return toFileSystemItems(ManagingFS.getInstance().getLocalRoots());
      }
      return findParentPsiDirectory();
    }

    private @NotNull Collection<PsiFileSystemItem> findParentPsiDirectory() {
      PsiFile file = getContainingFile();
      if (file != null) {
        VirtualFile virtualFile = file.getOriginalFile().getVirtualFile();
        VirtualFile parent = virtualFile.getParent();
        if (parent != null) {
          PsiDirectory directory = file.getManager().findDirectory(parent);
          if (directory != null) {
            return Collections.singleton(directory);
          }
        }
      }
      return Collections.emptyList();
    }
  }
}
