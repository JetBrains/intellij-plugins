// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Ref;
import com.intellij.testFramework.LightPlatformTestCase;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.after;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.before;

public final class OleasterTestUtil {
  public static void bootstrapLightPlatform() {
    Ref<OleasterLightPlatformTestCase> testCase = new Ref<>();
    before(() -> testCase.set(new OleasterLightPlatformTestCase()));
    after(() -> testCase.get().tearDown());
  }

  @SuppressWarnings({"JUnitTestCaseWithNoTests", "NewClassNamingConvention"})
  private static class OleasterLightPlatformTestCase extends LightPlatformTestCase {
    @SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
    private OleasterLightPlatformTestCase() throws Exception {
      setUp();
    }

    @Override
    protected boolean shouldContainTempFiles() {
      return false;
    }

    @Override
    public String getName() {
      return "testOleaster";
    }

    @Override
    public void tearDown() {
      ApplicationManager.getApplication().invokeAndWait(() -> {
        try {
          super.tearDown();
        }
        catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      });
    }
  }
}
