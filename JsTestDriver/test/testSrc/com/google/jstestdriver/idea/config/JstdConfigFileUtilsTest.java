package com.google.jstestdriver.idea.config;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdConfigFileUtilsTest extends TestCase {

  @Test
  public void testConvertPathToComponentList1() {
    String path = "/var/lib/jb/a.txt";
    List<String> str = JstdConfigFileUtils.convertPathToComponentList(path);
    Assert.assertEquals(Arrays.asList("", "var", "lib", "jb", "a.txt"), str);
  }

  @Test
  public void testConvertPathToComponentList2() {
    String path = "C:\\Users\\user\\IdeaProjects\\au";
    List<String> str = JstdConfigFileUtils.convertPathToComponentList(path);
    Assert.assertEquals(Arrays.asList("C:", "Users", "user", "IdeaProjects", "au"), str);
  }

  @Test
  public void testConvertPathToComponentList3() {
    String path = "test";
    List<String> str = JstdConfigFileUtils.convertPathToComponentList(path);
    Assert.assertEquals(Arrays.asList("test"), str);
  }

  @Test
  public void testConvertPathToComponentList4() {
    String path = "";
    List<String> str = JstdConfigFileUtils.convertPathToComponentList(path);
    Assert.assertTrue(str.isEmpty());
  }

  @Test
  public void testConvertPathToComponentList5() {
    String path = "test/www/qqq";
    List<String> str = JstdConfigFileUtils.convertPathToComponentList(path);
    Assert.assertEquals(Arrays.asList("test", "www", "qqq"), str);
  }

  @Test
  public void testConvertPathToComponentList6() {
    String path = "test\\www\\qqq";
    List<String> str = JstdConfigFileUtils.convertPathToComponentList(path);
    Assert.assertEquals(Arrays.asList("test", "www", "qqq"), str);
  }

  @Test
  public void testConvertPathToComponentList7() {
    String path = "///";
    List<String> str = JstdConfigFileUtils.convertPathToComponentList(path);
    Assert.assertEquals(Arrays.asList("", "", ""), str);
  }
}
