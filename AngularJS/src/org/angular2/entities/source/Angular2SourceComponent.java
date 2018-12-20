// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.SmartList;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.Angular2InjectionUtils;
import org.angular2.entities.Angular2Component;
import org.angular2.index.Angular2IndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.util.ObjectUtils.tryCast;
import static com.intellij.util.containers.ContainerUtil.addIfNotNull;

public class Angular2SourceComponent extends Angular2SourceDirective implements Angular2Component {

  public Angular2SourceComponent(@NotNull ES6Decorator decorator, @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @Nullable
  @Override
  public PsiFile getTemplateFile() {
    return getCachedValue(() -> create(
      findAngularComponentTemplate(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, getDecorator()));
  }

  @NotNull
  @Override
  public List<PsiFile> getCssFiles() {
    return getCachedValue(() -> create(findCssFiles(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, getDecorator()));
  }

  @Nullable
  private PsiFile findAngularComponentTemplate() {
    final JSProperty templateUrl = Angular2DecoratorUtil.getProperty(getDecorator(), Angular2IndexingHandler.TEMPLATE_URL);
    PsiFile file;
    if (templateUrl != null && (file = getReferencedFile(templateUrl.getValue(), true)) != null) {
      return file;
    }
    final JSProperty template = Angular2DecoratorUtil.getProperty(getDecorator(), Angular2IndexingHandler.TEMPLATE);
    if (template != null && (file = getReferencedFile(template.getValue(), false)) != null) {
      return file;
    }
    return null;
  }

  private List<PsiFile> findCssFiles() {
    List<PsiFile> result = new SmartList<>();
    final JSProperty styleUrls = Angular2DecoratorUtil.getProperty(getDecorator(), Angular2IndexingHandler.STYLE_URLS);
    JSArrayLiteralExpression list;
    if (styleUrls != null && (list = tryCast(styleUrls.getValue(), JSArrayLiteralExpression.class)) != null) {
      for (JSExpression expression : list.getExpressions()) {
        addIfNotNull(result, getReferencedFile(expression, true));
      }
    }
    final JSProperty styles = Angular2DecoratorUtil.getProperty(getDecorator(), Angular2IndexingHandler.STYLES);
    if (styles != null && (list = tryCast(styles.getValue(), JSArrayLiteralExpression.class)) != null) {
      for (JSExpression expression : list.getExpressions()) {
        addIfNotNull(result, getReferencedFile(expression, false));
      }
    }
    return result;
  }

  private static PsiFile getReferencedFile(@Nullable JSExpression expression, boolean directRefs) {
    if (expression != null) {
      PsiFile file;
      if (!directRefs && (file = Angular2InjectionUtils.getFirstInjectedFile(expression)) != null) {
        return file;
      }
      for (PsiReference ref : expression.getReferences()) {
        PsiElement el = ref.resolve();
        if (directRefs) {
          if (el instanceof PsiFile) {
            return (PsiFile)el;
          }
        }
        else if (el instanceof ES6ImportedBinding) {
          for (PsiElement importedElement : ((ES6ImportedBinding)el).findReferencedElements()) {
            if (importedElement instanceof PsiFile) {
              return (PsiFile)importedElement;
            }
          }
        }
      }
    }
    return null;
  }
}
