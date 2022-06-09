// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlFormInputWithoutLabelInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.*;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedFunctionInspection;
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedVariableInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.inspections.Angular2DecoratorInspectionsTest;
import org.angular2.inspections.Angular2TemplateInspectionsProvider;
import org.angular2.inspections.Angular2TemplateInspectionsTest;
import org.angularjs.AngularTestUtil;

import static java.util.Arrays.asList;

/**
 * @see Angular2DecoratorInspectionsTest
 * @see Angular2TemplateInspectionsTest
 */
public class InspectionsTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "inspections";
  }

  public void testUnusedSymbol() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class);
    myFixture.configureByFiles("unused.ts", "unused.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testUnusedSetter() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class);
    myFixture.configureByFiles("unusedSetter.ts", "unusedSetter.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testMethodCanBeStatic() {
    JSMethodCanBeStaticInspection canBeStaticInspection = new JSMethodCanBeStaticInspection();
    JSTestUtils.setInspectionHighlightLevel(getProject(), canBeStaticInspection, HighlightDisplayLevel.WARNING, getTestRootDisposable());
    myFixture.enableInspections(canBeStaticInspection);
    myFixture.configureByFiles("methodCanBeStatic.ts", "methodCanBeStatic.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testUnterminated() {
    myFixture.enableInspections(UnterminatedStatementJSInspection.class);
    myFixture.configureByFiles("unterminated.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testUnusedReference() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection.class,
                                JSUnusedLocalSymbolsInspection.class);
    myFixture.configureByFiles("unusedReference.html", "unusedReference.ts", "package.json");
    myFixture.checkHighlighting();

    for (String attrToRemove : asList("notUsedRef", "anotherNotUsedRef", "notUsedRefWithAttr", "anotherNotUsedRefWithAttr")) {
      AngularTestUtil.moveToOffsetBySignature("<caret>" + attrToRemove, myFixture);
      myFixture.launchAction(myFixture.findSingleIntention("Remove unused variable '" + attrToRemove + "'"));
    }
    myFixture.checkResultByFile("unusedReference.after.html");
  }

  public void testId() {
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection.class,
                                JSUnusedGlobalSymbolsInspection.class);
    myFixture.configureByFiles("object.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testPipeAndArgResolution() {
    myFixture.enableInspections(TypeScriptUnresolvedVariableInspection.class,
                                TypeScriptUnresolvedFunctionInspection.class);
    myFixture.configureByFiles("pipeAndArgResolution.html", "lowercase_pipe.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testHtmlTargetWithInterpolation() {
    myFixture.enableInspections(HtmlUnknownTargetInspection.class);
    myFixture.configureByFiles("htmlTargetWithInterpolation.html", "package.json");
    myFixture.checkHighlighting();
  }

  public void testGlobalThisInspection() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("top-level-this.html", "top-level-this.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testComplexGenerics() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("complex-generics.html", "complex-generics.ts","package.json");
    myFixture.checkHighlighting();
  }

  public void testDuplicateDeclarationOff() {
    myFixture.enableInspections(new JSDuplicatedDeclarationInspection());
    myFixture.configureByFiles("duplicateDeclarationOff.html", "duplicateDeclarationOff.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testDuplicateDeclarationOffTemplate() {
    myFixture.enableInspections(new JSDuplicatedDeclarationInspection());
    myFixture.configureByFiles("duplicateDeclarationOffLocalTemplate.ts", "package.json");
    myFixture.checkHighlighting();
  }

  public void testNestedComponentClasses() {
    myFixture.enableInspections(new Angular2TemplateInspectionsProvider());
    myFixture.configureByFiles("nested-classes.html", "nested-classes.ts","package.json");
    myFixture.checkHighlighting();
  }

  public void testEmptyVarDefinition() {
    myFixture.enableInspections(new JSUnusedLocalSymbolsInspection());
    myFixture.configureByFiles("package.json");
    myFixture.configureByText("template.html", "<ng-template ngFor let- ></ng-template>");
    myFixture.checkHighlighting();
  }

  public void testMissingLabelSuppressed() {
    myFixture.enableInspections(new HtmlFormInputWithoutLabelInspection());
    myFixture.configureByFiles("missingLabelSuppressed.html", "missingLabelSuppressed.ts", "package.json");
    myFixture.checkHighlighting();
  }
}
