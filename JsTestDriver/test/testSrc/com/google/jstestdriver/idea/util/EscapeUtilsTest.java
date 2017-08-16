package com.google.jstestdriver.idea.util;

import com.google.jstestdriver.idea.rt.util.EscapeUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class EscapeUtilsTest {

  @Test
  public void testJoinSplit() {
    List<String> strs = Arrays.asList("a", "", "aba", "abacaba", "_", "q", "\\1\\2\n");
    String encoded = EscapeUtils.join(strs, 'a');
    List<String> decoded = EscapeUtils.split(encoded, 'a');
    Assert.assertEquals(strs, decoded);
  }

  @Test
  public void testPath() {
    List<String> strs = Arrays.asList("C:\\dir\\file1", "C:\\dir\\file2");
    String encoded = EscapeUtils.join(strs, ',');
    List<String> decoded = EscapeUtils.split(encoded, ',');
    Assert.assertEquals(strs, decoded);
  }

  @Test
  public void testTrailingEmptyString() {
    List<String> list = Arrays.asList("hello", "");
    String encoded = EscapeUtils.join(list, ',');
    Assert.assertEquals("hello,", encoded);
    List<String> decoded = EscapeUtils.split(encoded, ',');
    Assert.assertEquals(list, decoded);
  }

  @Test
  public void testEmptyStrings() {
    List<String> list = Arrays.asList("", "", "");
    String encoded = EscapeUtils.join(list, '.');
    Assert.assertEquals("..", encoded);
    List<String> decoded = EscapeUtils.split(encoded, '.');
    Assert.assertEquals(list, decoded);
  }

  @Test
  public void testSeparatorStrings() {
    List<String> list = Arrays.asList(".", "..", "...");
    String encoded = EscapeUtils.join(list, '.');
    Assert.assertEquals("\\..\\.\\..\\.\\.\\.", encoded);
    List<String> decoded = EscapeUtils.split(encoded, '.');
    Assert.assertEquals(list, decoded);
  }
}
