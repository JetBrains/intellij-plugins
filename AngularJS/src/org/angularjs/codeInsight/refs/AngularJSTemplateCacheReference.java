// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.indexing.FileBasedIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularTemplateCacheIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTemplateCacheReference extends AngularJSReferenceBase<JSLiteralExpression> {
  public AngularJSTemplateCacheReference(@NotNull JSLiteralExpression element) {
    super(element, ElementManipulators.getValueTextRange(element));
  }

  @Override
  public Object @NotNull [] getVariants() {
    final Collection<String> keys = AngularIndexUtil.getAllKeys(AngularTemplateCacheIndex.TEMPLATE_CACHE_INDEX, getElement().getProject());
    return ArrayUtilRt.toStringArray(keys);
  }

  @Override
  public @Nullable PsiElement resolveInner() {
    final FileBasedIndex instance = FileBasedIndex.getInstance();
    final Project project = getElement().getProject();
    final String id = getCanonicalText();
    final Collection<VirtualFile> files = instance.getContainingFiles(AngularTemplateCacheIndex.TEMPLATE_CACHE_INDEX, id,
                                                                      GlobalSearchScope.allScope(project));
    final Ref<PsiElement> result = new Ref<>();
    for (VirtualFile file : files) {
      final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
      AngularTemplateCacheIndex.processTemplates(psiFile, attribute -> {
        if (id.equals(attribute.getValue())) {
          result.set(attribute.getValueElement());
        }
        return result.isNull();
      });
    }

    return result.get();
  }
}
