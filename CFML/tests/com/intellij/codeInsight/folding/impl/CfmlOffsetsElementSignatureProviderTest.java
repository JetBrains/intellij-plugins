// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.folding.impl;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class CfmlOffsetsElementSignatureProviderTest extends BasePlatformTestCase {
  private final OffsetsElementSignatureProvider myProvider = new OffsetsElementSignatureProvider();

  public void testMultiRootFile() {
    String text =
      """
        <cfcomponent>
            <cffunction name="f">
                <table>
                    <tbody>
                        <span id="my-id" <cfif shouldExpand>style="display:none;"</cfif>><a href="javascript:expand()">
                    </tbody>
                </table>
            </cffunction>
        </cfcomponent>
        """;
    myFixture.configureByText("test.cfc", text);

    int startOffset = text.indexOf("<cfif");
    int endOffset = text.indexOf("</cfif>");
    TextRange targetRange = TextRange.create(startOffset, endOffset);

    PsiElement e = myFixture.getFile().getViewProvider().findElementAt(startOffset, HTMLLanguage.INSTANCE);
    for (; e != null; e = e.getParent()) {
      if (targetRange.equals(e.getTextRange())) {
        break;
      }
    }
    assertNotNull(e);
    assertEquals(targetRange, e.getTextRange());

    String signature = myProvider.getSignature(e);
    assertNotNull(signature);

    assertSame(e, myProvider.restoreBySignature(e.getContainingFile(), signature, null));
  }
}
