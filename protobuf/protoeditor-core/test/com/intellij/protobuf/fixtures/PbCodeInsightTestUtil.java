// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.protobuf.fixtures;

import com.intellij.protobuf.TestUtils;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

/**
 * Code insight helpers for a JUnit 5 protoeditor test.
 * This is the JUnit 5 replacement of the deleted JUnit 3 base {@code PbCodeInsightFixtureTestCase}.
 */
@TestOnly
public final class PbCodeInsightTestUtil {
  private PbCodeInsightTestUtil() {
  }

  /**
   * Returns the protoeditor core test data directory, with a trailing slash.
   * Call it from any thread.
   */
  public static @NotNull String getTestDataPath() {
    return TestUtils.getProtobufCoreTestDataPath();
  }

  /**
   * Sets the test data path of {@code fixture} to the protoeditor core test data directory.
   * Call it in {@code @BeforeEach}, from any thread.
   */
  public static void setUpTestDataPath(@NotNull CodeInsightTestFixture fixture) {
    setUpTestDataPath(fixture, "");
  }

  /**
   * Sets the test data path of {@code fixture} to {@code subPath} in the protoeditor core test data directory.
   * For example, {@code "/ide/editing/"} gives the {@code ide/editing} directory.
   * Call it in {@code @BeforeEach}, from any thread.
   */
  public static void setUpTestDataPath(@NotNull CodeInsightTestFixture fixture, @NotNull String subPath) {
    fixture.setTestDataPath(getTestDataPath() + subPath);
  }
}
