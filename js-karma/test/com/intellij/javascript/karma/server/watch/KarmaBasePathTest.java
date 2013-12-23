package com.intellij.javascript.karma.server.watch;

import junit.framework.Assert;
import junit.framework.TestCase;

public class KarmaBasePathTest extends TestCase {

  public void testBraces() throws Exception {
    String baseDir = KarmaWatchPattern.extractBaseDir("/path/to/src/{,model/}*.js");
    Assert.assertEquals("/path/to/src", baseDir);
  }

  public void testWildcard() throws Exception {
    String baseDir = KarmaWatchPattern.extractBaseDir("/path/to/src/*.js");
    Assert.assertEquals("/path/to/src", baseDir);
  }

  public void testExclamationMark() throws Exception {
    String baseDir = KarmaWatchPattern.extractBaseDir("/path/to/src/!(qqq).js");
    Assert.assertEquals("/path/to/src", baseDir);
  }

  public void testPlusMark() throws Exception {
    String baseDir = KarmaWatchPattern.extractBaseDir("/path/to/src/+(qqq).js");
    Assert.assertEquals("/path/to/src", baseDir);
  }

  public void testQuestionMark() throws Exception {
    String baseDir = KarmaWatchPattern.extractBaseDir("/path/to/src/(qqq)?.js");
    Assert.assertEquals("/path/to/src", baseDir);
  }

}
