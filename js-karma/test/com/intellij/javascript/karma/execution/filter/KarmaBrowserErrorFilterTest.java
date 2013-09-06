package com.intellij.javascript.karma.execution.filter;

import com.intellij.javascript.karma.tree.KarmaBrowserErrorFilter;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Sergey Simonchik
 */
public class KarmaBrowserErrorFilterTest extends TestCase {

  public void testBasePath() throws Exception {
    String s = "at http://localhost:9876/base/spec/personSpec.js?1368878723000:22";
    KarmaBrowserErrorFilter.LinkInfo actual = KarmaBrowserErrorFilter.createLinkInfo(s);
    KarmaBrowserErrorFilter.LinkInfo expected = new KarmaBrowserErrorFilter.LinkInfo(
      3, s.length(), "spec/personSpec.js", 22
    );
    Assert.assertEquals(expected, actual);
  }

  public void testAbsolutePath() throws Exception {
    String s = "at http://localhost:9876/absolute/home/segrey/WebstormProjects/karma-chai-sample/test/test.js?1378466989000:1";
    KarmaBrowserErrorFilter.LinkInfo actual = KarmaBrowserErrorFilter.createLinkInfo(s);
    KarmaBrowserErrorFilter.LinkInfo expected = new KarmaBrowserErrorFilter.LinkInfo(
      3, s.length(), "/home/segrey/WebstormProjects/karma-chai-sample/test/test.js", 1
    );
    Assert.assertEquals(expected, actual);
  }

}
