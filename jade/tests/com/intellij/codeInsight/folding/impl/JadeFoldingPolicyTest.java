package com.intellij.codeInsight.folding.impl;

public class JadeFoldingPolicyTest extends AbstractFoldingPolicyTest {
  public void testNameWithSpecialCharacter() {
    doTest("""
             html
                 #container
                     p.
                       text""",
           "jade");
  }
}
