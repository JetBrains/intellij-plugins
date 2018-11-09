// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.Angular2Component;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.psi.util.CachedValueProvider.Result.create;

public class Angular2SourceComponent extends Angular2SourceDirective implements Angular2Component {

  private static final String TEMPLATE = "template";

  public Angular2SourceComponent(@NotNull ES6Decorator decorator, @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @Nullable
  @Override
  public HtmlFileImpl getHtmlTemplate() {
    return getCachedValue(() -> create(
      findAngularComponentTemplate(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, getDecorator()));
  }


  @Nullable
  private HtmlFileImpl findAngularComponentTemplate() {
    final JSProperty templateUrl = Angular2DecoratorUtil.getProperty(getDecorator(), AngularJSIndexingHandler.TEMPLATE_URL);
    if (templateUrl != null && templateUrl.getValue() != null) {
      for (PsiReference ref : templateUrl.getValue().getReferences()) {
        PsiElement el = ref.resolve();
        if (el instanceof HtmlFileImpl) {
          return (HtmlFileImpl)el;
        }
      }
    }
    final JSProperty template = Angular2DecoratorUtil.getProperty(getDecorator(), TEMPLATE);
    if (template != null && template.getValue() != null) {
      List<Pair<PsiElement, TextRange>> injections =
        InjectedLanguageManager.getInstance(getDecorator().getProject()).getInjectedPsiFiles(template.getValue());
      if (injections != null) {
        for (Pair<PsiElement, TextRange> injection : injections) {
          if (injection.getFirst() instanceof HtmlFileImpl) {
            return (HtmlFileImpl)injection.getFirst();
          }
        }
      }
    }
    return null;
  }
}
