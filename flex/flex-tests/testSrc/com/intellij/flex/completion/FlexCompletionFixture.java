// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.javascript.JSCompletionFixture;
import com.intellij.lang.javascript.JSCompletionTestHelperSimple;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSTestFixtures;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.lang.reflect.Method;

/**
 * The completion helpers of the JUnit 3 test {@code FlexCompletionTest} for a JUnit 5 test. Create it with {@link #flexCompletionFixture}.
 * A test can replace the completion call with {@link #setCompletionPerformer}, as the JUnit 3 test set {@code myCompletionPerformer}.
 * {@link #defaultTest()} completes in the {@code mxml} file of the test.
 */
@TestOnly
public class FlexCompletionFixture extends JSCompletionFixture {
  private Runnable myCompletionPerformer;

  public FlexCompletionFixture(@NotNull CodeInsightTestFixture fixture,
                               @NotNull Class<?> testClass,
                               @NotNull Method testMethod,
                               @NotNull JSFixtureOptions options) {
    super(fixture, testClass, testMethod, options);
  }

  @TestOnly
  public static @NotNull TestFixture<FlexCompletionFixture> flexCompletionFixture(@NotNull TestFixture<Project> projectFixture,
                                                                                 @NotNull JSFixtureOptions options) {
    return JSTestFixtures.jsFixture("flexCompletionFixture", projectFixture, options, FlexCompletionFixture::new);
  }

  @Override
  public void setUp() {
    super.setUp();
    myCompletionPerformer = () -> helper().superComplete();
  }

  /**
   * Replaces the completion call of the helpers. The default calls the completion of the JUnit 3 base once.
   */
  public void setCompletionPerformer(@Nullable Runnable completionPerformer) {
    myCompletionPerformer = completionPerformer;
  }

  @Override
  public @NotNull FlexJSCompletionTestHelper helper() {
    return new FlexJSCompletionTestHelper();
  }

  @Override
  public LookupElement @Nullable [] defaultTest() {
    return doTest("", "mxml");
  }

  public class FlexJSCompletionTestHelper extends JSCompletionTestHelperSimple {
    FlexJSCompletionTestHelper() {
      super(FlexCompletionFixture.this.getTestName(false), FlexCompletionFixture.this.getFixture(), FlexCompletionFixture.this.getExtension(),
            Boolean.TRUE.equals(FlexCompletionFixture.this.getOptions().getRecursionAssertions()), FlexCompletionFixture.this.getTestClass(),
            FlexCompletionFixture.this.getTestDataPath(), FlexCompletionFixture.this.getSmartCompletionTests());
    }

    @Override
    public LookupElement @Nullable [] complete() {
      if (myCompletionPerformer != null) {
        // The JUnit 3 test ran the performer on the EDT. A performer moves the caret and adds position markers.
        EdtTestUtil.runInEdtAndWait(() -> myCompletionPerformer.run());
      }
      return FlexCompletionFixture.this.getFixture().getLookupElements();
    }

    public void superComplete() {
      super.complete();
    }
  }
}
