// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.DesignerTests;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AbcFilterTest {
  private File out;
  private AbcFilter filter;
  private static final File TEST_LIB_DIR = new File(DesignerTests.getTestDataPath(), "abcTestLib");

  @Before
  public void runBeforeEveryTest() throws Exception {
    out = File.createTempFile("abc_", ".swf");
    filter = new AbcFilter();
  }

  @After
  public void runAfterEveryTest() {
    //noinspection ResultOfMethodCallIgnored
    out.delete();
  }

  @Test
  public void replaceMainClass() throws IOException {
    filter.filter(new File(TEST_LIB_DIR, "libraryWithIncompatibleMxFlexModuleFactory.swf"), out, null);
    Assertions.assertThat((int)out.length()).isEqualTo(409003);
  }

  @Test
  public void merge() throws IOException {
    filter.filter(new File(TEST_LIB_DIR, "MinimalComps_0_9_10.swf"), out, null);
    Assertions.assertThat((int)out.length()).isEqualTo(252500);
  }

  @Test
  public void fxg() throws IOException {
    File fxgFile = new File(DesignerTests.getTestDataPath(), "src/common/star.fxg");
    new FxgTranscoder().transcode(new FileInputStream(fxgFile), fxgFile.length(), out, false);

    //FileUtil.copy(out, new File("/Users/develar/test.swf"));
  }
}