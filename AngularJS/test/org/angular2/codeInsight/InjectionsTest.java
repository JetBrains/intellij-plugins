// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.TypeScriptTestUtil;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ExcludeFolder;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.util.ui.UIUtil;
import com.intellij.webSymbols.context.WebSymbolsContext;
import com.intellij.webSymbols.context.WebSymbolsContextProvider;
import com.intellij.webSymbols.context.impl.WebSymbolsContextProviderExtensionPoint;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.containers.ContainerUtil.prepend;
import static com.intellij.util.containers.ContainerUtil.sorted;
import static com.intellij.webSymbols.context.WebSymbolsContext.KIND_FRAMEWORK;
import static java.util.Arrays.asList;
import static org.angular2.modules.Angular2TestModule.*;
import static org.angularjs.AngularTestUtil.findOffsetBySignature;

public class InjectionsTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "injections";
  }

  public void testAngular2EmptyInterpolation() {
    myFixture.testCompletion("emptyInterpolation.html", "emptyInterpolation.after.html",
                             "package.json", "emptyInterpolation.ts");
  }

  public void testAngular2NonEmptyInterpolation() {
    myFixture.testCompletion("nonEmptyInterpolation.html", "nonEmptyInterpolation.after.html",
                             "package.json", "nonEmptyInterpolation.ts");
  }

  public void testEventHandler2Resolve() {
    myFixture.configureByFiles("event.html", "package.json", "event.ts");
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
  }

  public void testEventHandlerPrivate2Resolve() {
    myFixture.configureByFiles("event_private.html", "package.json", "event_private.ts");
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
  }

  public void testNgIfResolve() {
    myFixture.configureByFiles("ngIf.ts", "ng_if.ts", "package.json");
    checkVariableResolve("my_use<caret>r.last", "my_user", JSVariable.class);
  }

  private <T extends JSElement> void checkVariableResolve(final String signature, final String varName, final Class<T> varClass) {
    AngularTestUtil.checkVariableResolve(signature, varName, varClass, myFixture);
  }

  public void testStyles2() {
    myFixture.configureByFiles("custom.ts", "package.json");
    final int offset = findOffsetBySignature("Helvetica <caret>Neue", myFixture.getFile());
    final PsiElement element = InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), offset);
    assertEquals(CSSLanguage.INSTANCE, element.getLanguage());
  }

  public void testHost() {
    myFixture.configureByFiles("host.ts", "package.json");
    for (Pair<String, ? extends JSLanguageDialect> signature : List.of(Pair.create("eve<caret>nt", Angular2Language.INSTANCE),
                                                                       Pair.create("bind<caret>ing", Angular2Language.INSTANCE),
                                                                       Pair.create("at<caret>tribute",
                                                                                   JavaScriptSupportLoader.TYPESCRIPT))) {
      final int offset = findOffsetBySignature(signature.first, myFixture.getFile());
      PsiElement element = InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), offset);
      if (element == null) {
        element = myFixture.getFile().findElementAt(offset);
      }
      assertEquals(signature.first, signature.second, element.getContainingFile().getLanguage());
    }
  }

  public void testNonAngular() {
    myFixture.configureByFiles("nonAngularComponent.ts", "package.json");
    for (Pair<String, ? extends Language> signature : List.of(Pair.create("<foo><caret></foo>", HTMLLanguage.INSTANCE),
                                                              Pair.create("eve<caret>nt", JavaScriptSupportLoader.TYPESCRIPT),
                                                              Pair.create("bind<caret>ing", JavaScriptSupportLoader.TYPESCRIPT),
                                                              Pair.create("at<caret>tribute", JavaScriptSupportLoader.TYPESCRIPT))) {
      final int offset = findOffsetBySignature(signature.first, myFixture.getFile());
      PsiElement element = InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), offset);
      if (element == null) {
        element = myFixture.getFile().findElementAt(offset);
      }
      assertEquals(signature.first, signature.second, element.getContainingFile().getLanguage());
    }
  }

  public void testNgForExternalCompletion() {
    myFixture.testCompletion("ngFor.html", "ngFor.after.html", "package.json");
  }

  public void testNgForExternalResolve() {
    myFixture.configureByFiles("ngFor.after.html", "ngFor.ts", "ng_for_of.ts", "package.json");
    checkVariableResolve("\"myTo<caret>do\"", "myTodo", JSVariable.class);
  }

  public void testNgForInlineCompletion() {
    myFixture.testCompletion("ngFor.ts", "ngFor.after.ts", "package.json");
  }

  public void testNgForInlineResolve() {
    myFixture.configureByFiles("ngFor.after.ts", "ng_for_of.ts", "package.json");
    checkVariableResolve("\"myTo<caret>do\"", "myTodo", JSVariable.class);
  }

  public void test$EventExternalCompletion() {
    myFixture.testCompletion("$event.html", "$event.after.html", "package.json");
  }

  public void test$EventInlineCompletion() {
    myFixture.testCompletion("$event.ts", "$event.after.ts", "package.json");
  }

  public void testUserSpecifiedInjection() {
    myFixture.configureByFiles("userSpecifiedLang.ts", "package.json");
    for (Pair<String, String> signature : List.of(Pair.create("<div><caret></div>", Angular2HtmlLanguage.INSTANCE.getID()),
                                                  Pair.create("$text<caret>-color", "SCSS"),
                                                  //fails if correct order of injectors is not ensured
                                                  Pair.create("color: <caret>#00aa00", CSSLanguage.INSTANCE.getID()))) {

      final int offset = findOffsetBySignature(signature.first, myFixture.getFile());
      final PsiElement element = InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), offset);
      assertEquals(signature.first, signature.second, element.getContainingFile().getLanguage().getID());
    }
  }

  public void testNoInjectionInHTMLTemplateLiteral() {
    myFixture.configureByFiles("noInjection.html", "package.json");
    int offset = findOffsetBySignature("b<caret>ar", myFixture.getFile());
    assert offset > 0;
    PsiElement injection = InjectedLanguageManager.getInstance(getProject()).findInjectedElementAt(myFixture.getFile(), offset);
    assertNull("There should be no injection", injection);
  }

  public void testPrivateMembersOrder() {
    myFixture.configureByFiles("event_private.html", "event_private.ts", "package.json");
    myFixture.completeBasic();
    assertEquals("Private members should be sorted after public ones",
                 List.of("callSecuredApi", "callZ", "_callApi", "callA", "callAnonymousApi"),
                 myFixture.getLookupElementStrings());
  }

  public void testResolutionWithDifferentTemplateName() {
    myFixture.configureByFiles("event_different_name2.html", "package.json", "event_different_name.ts");
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
  }

  public void testIntermediateFoldersWithPackageJson1() {
    myFixture.configureByFiles("inner/event.html", "package.json", "inner/package.json", "inner/event.ts");
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
  }

  public void testIntermediateFoldersWithPackageJson2() {
    PsiFile[] files = myFixture.configureByFiles("inner/event.html", "inner/package.json", "inner/event.ts",
                                                 "inner2/event.html", "inner2/package.json", "inner2/event.ts");
    int offsetBySignature = findOffsetBySignature("callAnonymous<caret>Api()", myFixture.getFile());
    assertNull(myFixture.getFile().findReferenceAt(offsetBySignature));
    myFixture.openFileInEditor(files[3].getViewProvider().getVirtualFile());
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
  }

  public void testNodeModulesBasedInclusionCheck1() {
    myFixture.copyDirectoryToProject("node-modules-check", ".");

    myFixture.configureFromTempProjectFile("inner/event.html");
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);

    myFixture.configureFromTempProjectFile("inner2/event.html");
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
  }

  public void testAngularCliLibrary() {
    myFixture.copyDirectoryToProject("angular-cli-lib", ".");

    configureCopy(myFixture, ANGULAR_L10N_4_2_0);

    // Add "dist" folder to excludes
    ModuleRootModificationUtil.updateModel(getModule(), model -> {
      ExcludeFolder folder = model.getContentEntries()[0].addExcludeFolder(
        myFixture.getTempDirFixture().getFile("dist"));
      Disposer.register(myFixture.getProjectDisposable(), () ->
        ModuleRootModificationUtil.updateModel(getModule(), model2 ->
          model2.getContentEntries()[0].removeExcludeFolder(folder)));
    });

    // This should trigger immediate library update
    TypeScriptTestUtil.waitForLibraryUpdate(myFixture);

    // Ensure we are set up correctly
    ReadAction.run(() -> {
      assertTrue(ProjectFileIndex.getInstance(getProject()).isExcluded(
        myFixture.getTempDirFixture().getFile("dist/check")));
      assertTrue(ProjectFileIndex.getInstance(getProject()).isInLibrary(
        myFixture.getTempDirFixture().getFile("dist/my-common-ui-lib/package.json")));
    });

    myFixture.configureFromTempProjectFile("src/app/home.component.html");
    PsiElement transform = AngularTestUtil.resolveReference("| trans<caret>late", myFixture);
    assertEquals("translate.pipe.d.ts", transform.getContainingFile().getName());
    assertInstanceOf(transform, TypeScriptFunction.class);
  }

  public void testCustomContextProvider() {
    Disposable disposable = Disposer.newDisposable();
    WebSymbolsContext.WEB_SYMBOLS_CONTEXT_EP
      .getPoint()
      .registerExtension(
        new WebSymbolsContextProviderExtensionPoint(KIND_FRAMEWORK, "angular", new WebSymbolsContextProvider() {
          @NotNull
          @Override
          public CachedValueProvider.Result<Integer> isEnabled(@NotNull Project project, @NotNull VirtualFile directory) {
            return CachedValueProvider.Result.create(1, ModificationTracker.EVER_CHANGED);
          }
        }),
        disposable);
    myFixture.configureByFiles("inner/event.html", "inner/package.json", "inner/event.ts");
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
    Disposer.dispose(disposable);

    // Force reload of roots
    Angular2LangUtil.isAngular2Context(myFixture.getFile());
    UIUtil.dispatchAllInvocationEvents();

    int offsetBySignature = findOffsetBySignature("callAnonymous<caret>Api()", myFixture.getFile());
    assertNull(myFixture.getFile().findReferenceAt(offsetBySignature));
  }

  public void testTemplateReferencedThroughImportStatement() {
    myFixture.configureByFiles("event_private.html", "package.json", "event_private.import.ts");
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
  }

  public void testUnclosedTemplateAttribute() {
    myFixture.configureByFiles("unclosedTemplate.html", "package.json");
    assertInstanceOf(myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent(),
                     Angular2HtmlTemplateBindings.class);
  }

  public void testMultiPartTemplateString() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class);
    myFixture.configureByFiles("multipart-template-string.ts", "package.json");
    myFixture.checkHighlighting(true, false, true);
  }

  public void testCompletionOnTemplateReferenceVariable() {
    configureCopy(myFixture, ANGULAR_CORE_4_0_0);
    myFixture.configureByFiles("ref-var.html", "ref-var.ts");
    List<String> defaultProps = asList("constructor", "hasOwnProperty", "isPrototypeOf", "propertyIsEnumerable",
                                       "toLocaleString", "toString", "valueOf");
    for (Pair<String, List<String>> check : asList(
      pair("comp", prepend(defaultProps, "myCompProp")),
      pair("dir", prepend(defaultProps, "myDirectiveProp")),
      pair("template", prepend(defaultProps, "createEmbeddedView", "elementRef"))
    )) {
      AngularTestUtil.moveToOffsetBySignature(check.first + "Ref.<caret>", myFixture);
      myFixture.completeBasic();
      assertEquals("Issue with " + check.first,
                   sorted(check.second),
                   sorted(myFixture.getLookupElementStrings()));
    }
  }

  public void testInterpolationTyping() {
    myFixture.configureByFiles("interpolation.html", "package.json");
    myFixture.type("{{foo");
    Document doc = myFixture.getDocument(myFixture.getFile());
    assertEquals("<div>{{foo}}</div>", doc.getText());
    WriteAction.runAndWait(() -> PsiDocumentManager.getInstance(getProject()).commitDocument(doc));
    myFixture.type("}}bar");
    assertEquals("<div>{{foo}}bar</div>", doc.getText());
  }

  public void testTopLevelThisCompletion() {
    myFixture.configureByFiles("top-level-this.ts", "package.json");
    myFixture.completeBasic();
    assertSameElements(sorted(myFixture.getLookupElementStrings()), sorted(asList("getSomething", "$any", "ref1", "title")));
  }
}
