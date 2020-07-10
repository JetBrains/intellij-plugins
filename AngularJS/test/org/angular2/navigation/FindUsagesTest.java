// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationOrUsageHandler2;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindUsagesTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "/findUsages";
  }

  public void testPrivateComponentField() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("f<caret>oo",
                "foo <private.html:(3,6):(0,3)>",
                "foo <private.html:(69,72):(0,3)>",
                "this.foo <private.ts:(350,358):(5,8)>");
  }

  public void testPrivateComponentMethod() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("b<caret>ar",
                "bar() <private.html:(13,16):(0,3)>",
                "bar() <private.html:(49,52):(0,3)>",
                "this.bar() <private.ts:(369,377):(5,8)>");
  }

  public void testPrivateConstructorField() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("fooB<caret>ar",
                "foo + fooBar <private.html:(120,126):(0,6)>",
                "fooBar <private.html:(25,31):(0,6)>",
                "this.fooBar <private.ts:(385,396):(5,11)>");
  }

  public void testComponentCustomElementSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json");
    checkUsages("slots-<caret>component",
                "slots-component <slots.test.component.html:(0,142):(1,16)>",
                "slots-component <slots.test.component.html:(0,142):(126,141)>");
  }

  public void testComponentStandardElementSelector() {
    myFixture.configureByFiles("standardSelectors.ts", "package.json");
    AngularTestUtil.moveToOffsetBySignature("\"di<caret>v,", myFixture);
    // Cannot find usages of standard tags and attributes, just check outcome
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD);
  }

  public void testComponentStandardAttributeSelector() {
    myFixture.configureByFiles("standardSelectors.ts", "package.json");
    AngularTestUtil.moveToOffsetBySignature(",[cl<caret>ass]", myFixture);
    // Cannot find usages of standard tags and attributes, just check outcome
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD);
  }

  public void testSlotComponentElementSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json");
    checkUsages("tag<caret>-slot",
                "tag-slot <slots.test.component.html:(20,70):(1,9)>",
                "tag-slot <slots.test.component.html:(20,70):(41,49)>");
  }

  public void testSlotComponentAttributeSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json");
    checkUsages("attr<caret>-slot",
                "attr-slot <slots.test.component.html:(78,87):(0,9)>");
  }

  private void checkUsages(@NotNull String signature,
                           String @NotNull ... usages) {
    AngularTestUtil.moveToOffsetBySignature(signature, myFixture);
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.SU);
    assertEquals(Stream.of(usages)
                   .sorted()
                   .collect(Collectors.toList()),
                 myFixture.findUsages(myFixture.getElementAtCaret())
                   .stream()
                   .map(usage -> getElementText(usage.getElement()) +
                                 " <" + usage.getFile().getName() +
                                 ":" + usage.getElement().getTextRange() +
                                 ":" + usage.getRangeInElement() +
                                 ">")
                   .sorted()
                   .collect(Collectors.toList())
    );
  }

  private void checkGTDUOutcome(@Nullable GotoDeclarationOrUsageHandler2.GTDUOutcome gtduOutcome) {
    PsiFile file = myFixture.getFile();
    int offset = myFixture.getCaretOffset();
    @SuppressWarnings("deprecation")
    Editor editor = InjectedLanguageUtil.getEditorForInjectedLanguageNoCommit(myFixture.getEditor(), file);
    if (editor instanceof EditorWindow) {
      file = ((EditorWindow)editor).getInjectedFile();
      offset -= InjectedLanguageManager.getInstance(myFixture.getProject()).injectedToHost(file, 0);
    }
    assertEquals(gtduOutcome,
                 GotoDeclarationOrUsageHandler2.testGTDUOutcome(editor, file, offset));
  }

  private static String getElementText(PsiElement element) {
    if (element instanceof XmlTag) {
      return ((XmlTag)element).getName();
    }
    else if (element instanceof XmlAttribute) {
      return element.getText();
    }
    return element.getParent().getText();
  }
}
