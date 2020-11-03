// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.lang.javascript.psi.impl.FlexPropertyReference;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.properties.BundleNameEvaluator;
import com.intellij.lang.properties.PropertiesReferenceManager;
import com.intellij.lang.properties.ResourceBundleReference;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.references.PropertyReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class FlexPropertiesSupport {
  private static final BundleNameEvaluator MY_BUNDLE_NAME_EVALUATOR = new BundleNameEvaluator() {
    @Override
    public String evaluateBundleName(PsiFile psiFile) {
      final VirtualFile virtualFile = psiFile == null ? null : psiFile.getOriginalFile().getVirtualFile();
      if (virtualFile != null && psiFile instanceof PropertiesFile) {
        String className = virtualFile.getNameWithoutExtension();
        String packageName = JSResolveUtil.getExpectedPackageNameFromFile(virtualFile, psiFile.getProject());
        if (!StringUtil.isEmpty(packageName)) {
          className = packageName + "." + className;
        }
        return className;
      }
      return null;
    }
  };

  public static <T extends PsiElement> PsiReference[] getPropertyReferences(T element, PropertyReferenceInfoProvider<T> infoProvider) {
    TextRange range = infoProvider.getReferenceRange(element);
    if (range == null) return PsiReference.EMPTY_ARRAY;

    return new PsiReference[]{
      new MyPropertyReference(range, element, infoProvider)
    };
  }

  public static <T extends PsiElement> PsiReference[] getResourceBundleReference(T element, BundleReferenceInfoProvider<T> infoProvider) {
    final TextRange textRange = infoProvider.getReferenceRange(element);
    if (textRange == null) return PsiReference.EMPTY_ARRAY;

    return new PsiReference[] { new MyResourceBundleReference(element, infoProvider.isSoft(element)) {
      @Override
      protected TextRange calculateDefaultRangeInElement() {
        return textRange;
      }
    }
    };
  }

  public interface PropertyReferenceInfoProvider<T> {
    @Nullable TextRange getReferenceRange(T element);

    @Nullable String getBundleName(T element);

    boolean isSoft(T element);
  }

  public interface BundleReferenceInfoProvider<T> {
    @Nullable TextRange getReferenceRange(T element);
    boolean isSoft(T element);
  }

  private static class MyPropertyReference<T extends PsiElement> extends PropertyReference implements FlexPropertyReference {
    MyPropertyReference(TextRange range, T element, PropertyReferenceInfoProvider<T> infoProvider) {
      super(range.substring(element.getText()), element, infoProvider.getBundleName(element), infoProvider.isSoft(element), range);
    }

    @Override
    protected List<PropertiesFile> retrievePropertyFilesByBundleName(String bundleName, PsiElement element) {
      if (bundleName == null) return Collections.emptyList();
      return PropertiesReferenceManager.getInstance(element.getProject()).findPropertiesFiles(
        element.getResolveScope(),
        bundleName,
        MY_BUNDLE_NAME_EVALUATOR
      );
    }
  }

  private static class MyResourceBundleReference extends ResourceBundleReference implements EmptyResolveMessageProvider {
    MyResourceBundleReference(PsiElement element, boolean soft) {
      super(element, soft);
    }

    @Override
    @NotNull
    public String getUnresolvedMessagePattern() {
      return "Cannot resolve property bundle";
    }

    @Override
    @Nullable
    public String evaluateBundleName(final PsiFile psiFile) {
      return MY_BUNDLE_NAME_EVALUATOR.evaluateBundleName(psiFile);
    }
  }

}
