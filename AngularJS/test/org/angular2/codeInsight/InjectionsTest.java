// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings;
import org.angularjs.AngularTestUtil;

import java.util.List;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.containers.ContainerUtil.prepend;
import static com.intellij.util.containers.ContainerUtil.sorted;
import static java.util.Arrays.asList;
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
    for (Pair<String, ? extends JSLanguageDialect> signature : ContainerUtil.newArrayList(
      Pair.create("eve<caret>nt", Angular2Language.INSTANCE),
      Pair.create("bind<caret>ing", Angular2Language.INSTANCE),
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
    for (Pair<String, String> signature : ContainerUtil.newArrayList(
      Pair.create("<div><caret></div>", Angular2HtmlLanguage.INSTANCE.getID()),
      Pair.create("$text<caret>-color", "SCSS"), //fails if correct order of injectors is not ensured
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
    assertEquals("Private members should be sorted after public ones", myFixture.getLookupElementStrings(),
                 ContainerUtil.newArrayList("callSecuredApi", "callZ", "_callApi", "callA", "callAnonymousApi"));
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
    VirtualFile root = myFixture.copyDirectoryToProject("node-modules-check", ".");

    VirtualFile nodeModules1 = root.findFileByRelativePath("inner2/node_modules/@angular/core");
    PsiTestUtil.addContentRoot(myModule, nodeModules1);
    try {
      myFixture.openFileInEditor(root.findFileByRelativePath("inner/event.html"));
      int offsetBySignature = findOffsetBySignature("callAnonymous<caret>Api()", myFixture.getFile());
      assertNull(myFixture.getFile().findReferenceAt(offsetBySignature));

      myFixture.openFileInEditor(root.findFileByRelativePath("inner2/event.html"));
      checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction.class);
    }
    finally {
      ModuleRootModificationUtil.updateModel(myModule, model -> model.removeContentEntry(
        ContainerUtil.find(model.getContentEntries(), entry -> nodeModules1.equals(entry.getFile()))));
    }
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
    myFixture.copyDirectoryToProject("node_modules", "./node_modules");
    myFixture.configureByFiles("ref-var.html", "ref-var.ts", "package.json");
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
}
