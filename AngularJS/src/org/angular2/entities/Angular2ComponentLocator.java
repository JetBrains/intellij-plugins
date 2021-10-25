// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.StreamEx;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;

import static java.util.Collections.emptyList;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.index.Angular2IndexingHandler.resolveComponentsFromIndex;

public final class Angular2ComponentLocator {

  public static @Nullable TypeScriptClass findComponentClass(@NotNull PsiElement templateContext) {
    return ContainerUtil.getFirstItem(findComponentClasses(templateContext));
  }

  public static @NotNull List<@NotNull TypeScriptClass> findComponentClasses(@NotNull PsiElement templateContext) {
    final PsiFile file = templateContext.getContainingFile();
    if (file == null
        || !(file.getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE)
             || file.getLanguage().is(Angular2Language.INSTANCE)
             || isStylesheet(file))) {
      return emptyList();
    }
    PsiFile hostFile = getHostFile(templateContext);
    if (hostFile == null) {
      return emptyList();
    }
    if (!file.getOriginalFile().equals(hostFile) && DialectDetector.isTypeScript(hostFile)) {
      // inline content
      return ContainerUtil.packNullables(getClassForDecoratorElement(
        InjectedLanguageManager.getInstance(templateContext.getProject()).getInjectionHost(file.getOriginalFile())));
    }
    // external content
    List<TypeScriptClass> result = new SmartList<>(
      StreamEx.of(Angular2FrameworkHandler.EP_NAME.getExtensionList())
        .toFlatList(h -> h.findAdditionalComponentClasses(hostFile)));
    if (result.isEmpty() || isStylesheet(file)) {
      result.addAll(resolveComponentsFromSimilarFile(hostFile));
    }
    if (result.isEmpty() || isStylesheet(file)) {
      result.addAll(resolveComponentsFromIndex(hostFile, dec -> hasFileReference(dec, hostFile)));
    }
    return result;
  }

  public static @NotNull List<@NotNull TypeScriptClass> findComponentClassesInFile(
    @NotNull PsiFile file, @Nullable BiPredicate<TypeScriptClass, ES6Decorator> filter) {
    return StreamEx.of(JSStubBasedPsiTreeUtil.findDescendants(file, Angular2IndexingHandler.TS_CLASS_TOKENS))
      .select(TypeScriptClass.class)
      .filter(cls -> {
        ES6Decorator dec = findDecorator(cls, COMPONENT_DEC);
        return dec != null && (filter == null || filter.test(cls, dec));
      })
      .toList();
  }

  private static @NotNull List<TypeScriptClass> resolveComponentsFromSimilarFile(@NotNull PsiFile file) {
    final String name = file.getViewProvider().getVirtualFile().getNameWithoutExtension();
    final PsiDirectory dir = file.getParent();
    if (dir == null) return emptyList();
    for (String ext : TypeScriptUtil.TYPESCRIPT_EXTENSIONS_WITHOUT_DECLARATIONS) {
      final PsiFile directiveFile = dir.findFile(name + ext);

      if (directiveFile != null) {
        return findComponentClassesInFile(directiveFile, (cls, dec) -> hasFileReference(dec, file));
      }
    }
    
    return emptyList();
  }

  private static boolean hasFileReference(@Nullable ES6Decorator componentDecorator, @NotNull PsiFile file) {
    Angular2Component component = Angular2EntitiesProvider.getComponent(componentDecorator);
    if (component != null) {
      return isStylesheet(file) ? component.getCssFiles().contains(file) : file.equals(component.getTemplateFile());
    }
    return false;
  }

  public static boolean isStylesheet(@NotNull PsiFile file) {
    return file instanceof StylesheetFile;
  }

  private static @Nullable PsiFile getHostFile(@NotNull PsiElement context) {
    final PsiElement original = CompletionUtil.getOriginalOrSelf(context);
    PsiFile hostFile = FileContextUtil.getContextFile(original != context ? original : context.getContainingFile().getOriginalFile());
    return hostFile != null ? hostFile.getOriginalFile() : null;
  }
}
