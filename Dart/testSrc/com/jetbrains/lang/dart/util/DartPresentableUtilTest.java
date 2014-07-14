package com.jetbrains.lang.dart.util;

import junit.framework.TestCase;

public class DartPresentableUtilTest extends TestCase {

  public void testUnwrapCommentDelimiters1() {
    String unwrapped = DartPresentableUtil.unwrapCommentDelimiters("/** ABC\n * DEF \n */ ");
    assertEquals("ABC\nDEF \n ", unwrapped);

    unwrapped = DartPresentableUtil.unwrapCommentDelimiters("   /**ABC \n*DEF\nGHI */ ");
    assertEquals("ABC \nDEF\nGHI ", unwrapped);

    unwrapped = DartPresentableUtil.unwrapCommentDelimiters("   /**ABC \n*DEF\n * GHI */ ");
    assertEquals("ABC \nDEF\nGHI ", unwrapped);
  }

  public void testUnwrapCommentDelimiters2() {
    String unwrapped = DartPresentableUtil.unwrapCommentDelimiters("/* ABC\n * DEF \n */ ");
    assertEquals("ABC\nDEF \n ", unwrapped);

    unwrapped = DartPresentableUtil.unwrapCommentDelimiters("   /*ABC \n*DEF\nGHI */ ");
    assertEquals("ABC \nDEF\nGHI ", unwrapped);

    unwrapped = DartPresentableUtil.unwrapCommentDelimiters("   /*ABC \n*DEF\n * GHI */ ");
    assertEquals("ABC \nDEF\nGHI ", unwrapped);
  }

  public void testUnwrapCommentDelimiters3() {
    String unwrapped = DartPresentableUtil.unwrapCommentDelimiters("/// ABC\n /// DEF \n /// ");
    assertEquals("ABC\nDEF \n", unwrapped);

    unwrapped = DartPresentableUtil.unwrapCommentDelimiters("   ///ABC \n///DEF\n///GHI ");
    assertEquals("ABC \nDEF\nGHI ", unwrapped);

    unwrapped = DartPresentableUtil.unwrapCommentDelimiters("   ///ABC \n///DEF\n ///GHI ");
    assertEquals("ABC \nDEF\nGHI ", unwrapped);
  }

  public void testUnwrapCommentDelimiters4() {
    String unwrapped = DartPresentableUtil.unwrapCommentDelimiters("// ABC\n // DEF \n // ");
    assertEquals("ABC\nDEF \n", unwrapped);

    unwrapped = DartPresentableUtil.unwrapCommentDelimiters("   //ABC \n//DEF\n//GHI ");
    assertEquals("ABC \nDEF\nGHI ", unwrapped);

    unwrapped = DartPresentableUtil.unwrapCommentDelimiters("   //ABC \n//DEF\n //GHI ");
    assertEquals("ABC \nDEF\nGHI ", unwrapped);
  }

}
