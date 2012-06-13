package com.google.jstestdriver.idea.util;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class EscapeUtilsTest {

  @org.junit.Test
  public void testJoinSplit() throws Exception {
    List<String> strs = Arrays.asList("a", "", "aba", "abacaba", "_", "q", "\\1\\2\n");
    String encoded = EscapeUtils.join(strs, 'a');
    System.out.println("encoded: " + encoded);
    List<String> decoded = EscapeUtils.split(encoded, 'a');
    System.out.println("decoded: " + decoded);
    Assert.assertEquals(strs, decoded);
  }

  @org.junit.Test
  public void testPath() throws Exception {
    List<String> strs = Arrays.asList("C:\\dir\\file1", "C:\\dir\\file2");
    String encoded = EscapeUtils.join(strs, ',');
    System.out.println("encoded: " + encoded);
    List<String> decoded = EscapeUtils.split(encoded, ',');
    System.out.println("decoded: " + decoded);
    Assert.assertEquals(strs, decoded);
  }

  @Test
  public void testTrailingEmptyString() throws Exception {
    List<String> list = Arrays.asList("hello", "");
    String encoded = EscapeUtils.join(list, ',');
    Assert.assertEquals("hello,", encoded);
    System.out.println("encoded: " + encoded);
    List<String> decoded = EscapeUtils.split(encoded, ',');
    System.out.println("decoded: " + decoded);
    Assert.assertEquals(list, decoded);
  }
}
