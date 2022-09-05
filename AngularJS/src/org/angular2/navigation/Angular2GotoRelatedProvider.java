// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.openapi.util.NlsContexts.ListItem;
import com.intellij.openapi.util.NlsContexts.Separator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testIntegration.TestFinderHelper;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import org.angular2.Angular2InjectionUtils;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2ComponentLocator;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Module;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.intellij.util.containers.ContainerUtil.*;

public class Angular2GotoRelatedProvider extends GotoRelatedProvider {

  private static final int COMPONENT_INDEX = 1;
  private static final int TEMPLATE_INDEX = 2;
  private static final int TEST_INDEX = 3;
  private static final int STYLES_INDEX_START = 4;
  private static final int MODULE_INDEX = 5;

  @Override
  public @NotNull List<? extends GotoRelatedItem> getItems(@NotNull PsiElement psiElement) {
    PsiFile file = psiElement.getContainingFile();
    if (file == null || !Angular2LangUtil.isAngular2Context(file)) {
      return Collections.emptyList();
    }
    List<TypeScriptClass> componentClasses = new SmartList<>();
    if (DialectDetector.isTypeScript(file)) {
      addIfNotNull(componentClasses, PsiTreeUtil.getParentOfType(psiElement, TypeScriptClass.class));
      if (componentClasses.isEmpty()) {
        for (PsiElement el : TestFinderHelper.findClassesForTest(file)) {
          if (el instanceof JSFile) {
            componentClasses.addAll(PsiTreeUtil.getStubChildrenOfTypeAsList(el, TypeScriptClass.class));
          }
        }
      }
    }
    else {
      componentClasses.addAll(Angular2ComponentLocator.findComponentClasses(file));
    }

    PsiFile filter = ObjectUtils.notNull(
      Angular2InjectionUtils.getFirstInjectedFile(PsiTreeUtil.getParentOfType(psiElement, JSExpression.class)),
      file);

    List<Angular2Component> components = mapNotNull(componentClasses, Angular2EntitiesProvider::getComponent);
    return switch (components.size()) {
      case 0 -> Collections.emptyList();
      case 1 -> filter(getRelatedItems(components.get(0)),
                       f -> !filter.equals(ObjectUtils.doIfNotNull(
                         f.getElement(), PsiElement::getContainingFile)));
      default -> map(components, c -> new GotoRelatedItem(Objects.requireNonNull(c.getTypeScriptClass()), getGroupName()));
    };
  }

  private static List<GotoRelatedItem> getRelatedItems(Angular2Component component) {
    List<GotoRelatedItem> result = new SmartList<>();
    TypeScriptClass cls = component.getTypeScriptClass();
    if (cls != null && cls.getName() != null) {
      result.add(new Angular2GoToRelatedItem(cls, COMPONENT_INDEX, false,
                                             Angular2Bundle.message("angular.action.goto-related.component-class")));
    }
    PsiFile file = component.getTemplateFile();
    if (file != null) {
      result.add(new Angular2GoToRelatedItem(file, TEMPLATE_INDEX, true,
                                             Angular2Bundle.message("angular.action.goto-related.template")));
    }
    boolean first = true;
    int count = 1;
    Collection<PsiElement> tests = TestFinderHelper.findTestsForClass(component.getSourceElement());
    for (PsiElement el : tests) {
      result.add(new Angular2GoToRelatedItem(el, first ? TEST_INDEX : -1, false,
                                             Angular2Bundle.message("angular.action.goto-related.tests",
                                                                    tests.size() == 1 ? "" : " " + count++)));
      first = false;
    }

    List<PsiFile> cssFiles = component.getCssFiles();
    int mnemonic = STYLES_INDEX_START;
    count = 1;
    for (PsiFile cssFile : cssFiles) {
      result.add(new Angular2GoToRelatedItem(cssFile, mnemonic++, true,
                                             Angular2Bundle.message("angular.action.goto-related.styles",
                                                                    cssFiles.size() == 1 ? "" : " " + count++)));
    }
    first = true;
    for (TypeScriptClass moduleClass : mapNotNull(component.getAllDeclaringModules(), Angular2Module::getTypeScriptClass)) {
      if (moduleClass.getName() != null) {
        result.add(new Angular2GoToRelatedItem(moduleClass, first ? MODULE_INDEX : -1, false,
                                               Angular2Bundle.message("angular.action.goto-related.module")));
        first = false;
      }
    }
    return result;
  }

  private static @Separator String getGroupName() {
    return Angular2Bundle.message("angular.action.goto-related.group-name");
  }

  private static final class Angular2GoToRelatedItem extends GotoRelatedItem {

    private final @Nls String myContainerName;
    private final @ListItem String myName;

    private Angular2GoToRelatedItem(@NotNull PsiElement element,
                                    int mnemonic,
                                    boolean inlineable,
                                    @Nullable @ListItem String name) {
      super(element, getGroupName(), mnemonic > 9 ? -1 : mnemonic);
      myContainerName = inlineable && InjectedLanguageManager.getInstance(element.getProject())
                                        .getTopLevelFile(element) != element.getContainingFile()
                        ? Angular2Bundle.message("angular.action.goto-related.inline")
                        : null;
      myName = name;
    }

    @Override
    public @Nullable String getCustomName() {
      return myName;
    }

    @Override
    public String getCustomContainerName() {
      return myContainerName;
    }
  }
}
