// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.entities.*;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angular2.inspections.AngularMissingOrInvalidDeclarationInModuleInspection;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class FrameworkHandlerTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "frameworkHandler";
  }

  public void testAdditionalComponentClasses() {
    Disposable disposable = Disposer.newDisposable();
    Disposer.register(myFixture.getTestRootDisposable(), disposable);
    Angular2FrameworkHandler.EP_NAME.getPoint().registerExtension(new Angular2FrameworkHandler() {
      @Override
      public @NotNull List<TypeScriptClass> findAdditionalComponentClasses(@NotNull PsiFile context) {
        PsiFile componentsFile = PsiManager.getInstance(getProject()).findFile(myFixture.findFileInTempDir("components.ts"));
        return Angular2ComponentLocator.findComponentClassesInFile(componentsFile, (cls, dec) -> "FooComponent".equals(cls.getName()));
      }
    }, disposable);

    myFixture.configureByFiles("components.ts", "package.json");
    myFixture.configureByText("test.html", "{{ <caret> }}");
    assertSize(1, Angular2ComponentLocator.findComponentClasses(myFixture.getFile()));
    myFixture.completeBasic();
    assertOrderedEquals(ContainerUtil.sorted(myFixture.getLookupElementStrings()), "$any", "fooField1", "fooField2");

    Disposer.dispose(disposable);
    assertSize(0, Angular2ComponentLocator.findComponentClasses(myFixture.getFile()));
    myFixture.completeBasic();
    assertOrderedEquals(ContainerUtil.sorted(myFixture.getLookupElementStrings()), "$any");
  }

  public void testSelectModuleForDeclarationsScope() {
    Disposable disposable = Disposer.newDisposable();
    Disposer.register(myFixture.getTestRootDisposable(), disposable);
    Angular2FrameworkHandler.EP_NAME.getPoint().registerExtension(new Angular2FrameworkHandler() {
      @Override
      public @NotNull Angular2Module selectModuleForDeclarationsScope(@NotNull Collection<@NotNull Angular2Module> modules,
                                                                      @NotNull Angular2Component component,
                                                                      @NotNull PsiFile context) {
        return ContainerUtil.find(modules, module -> module.getName().equals("FooModule"));
      }
    }, disposable);

    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("multi-module.ts", "package.json");
    myFixture.configureByText("template.html",
                              "<app-foo></app-foo>\n" +
                              "<<error descr=\"Component or directive matching app-bar element is out of the current Angular module's scope\">app-bar</error>></app-bar>\n" +
                              "<<warning descr=\"Unknown html tag foobar\">foobar</warning>></<warning descr=\"Unknown html tag foobar\">foobar</warning>>");
    myFixture.checkHighlighting();

    Disposer.dispose(disposable);
    myFixture.configureByText("template.html",
                              "<<error descr=\"Component or directive matching app-foo element is out of the current Angular module's scope\">app-foo</error>></app-foo>\n" +
                              "<app-bar></app-bar>\n" +
                              "<<warning descr=\"Unknown html tag foobar\">foobar</warning>></<warning descr=\"Unknown html tag foobar\">foobar</warning>>");
    myFixture.checkHighlighting();
  }

  public void testSuppressModuleInspectionErrors() {
    Angular2FrameworkHandler.EP_NAME.getPoint().registerExtension(new Angular2FrameworkHandler() {
      @Override
      public boolean suppressModuleInspectionErrors(@NotNull Collection<@NotNull Angular2Module> modules,
                                                    @NotNull Angular2Declaration declaration) {
        return "FooComponent".equals(declaration.getName());
      }
    }, myFixture.getTestRootDisposable());

    myFixture.enableInspections(new AngularMissingOrInvalidDeclarationInModuleInspection());
    myFixture.configureByFiles("multi-module-errors.ts", "package.json");
    myFixture.checkHighlighting();
  }
}
