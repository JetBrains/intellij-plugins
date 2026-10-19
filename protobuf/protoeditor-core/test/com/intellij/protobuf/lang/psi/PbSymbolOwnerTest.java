// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.protobuf.lang.psi;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.psi.util.QualifiedName;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link PbSymbolOwner}. */
@TestApplication
public class PbSymbolOwnerTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp() {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
  }

  @Test
  public void testFindSymbolsByQualifiedNameReturnsMatch() {
    PbFile file = configure("syntax = 'proto2';", "message Msg {", "  message Sub {}", "}");
    Collection<PbMessageType> symbols = ReadAction.computeBlocking(
      () -> file.findSymbols(QualifiedName.fromDottedString("Msg.Sub"), PbMessageType.class));
    assertEquals(1, symbols.size());
  }

  @Test
  public void testFindSymbolsByQualifiedNameReturnsEmptyForMissingName() {
    PbFile file = configure("syntax = 'proto2';", "message Msg {}");
    Collection<PbSymbol> symbols = ReadAction.computeBlocking(
      () -> file.findSymbols(QualifiedName.fromDottedString("Missing")));
    assertTrue(symbols.isEmpty());
    Collection<PbMessageType> typedSymbols = ReadAction.computeBlocking(
      () -> file.findSymbols(QualifiedName.fromDottedString("Msg.Missing"), PbMessageType.class));
    assertTrue(typedSymbols.isEmpty());
  }

  private PbFile configure(String... lines) {
    return (PbFile)myFixture.configureByText(PbFileType.INSTANCE, String.join("\n", lines));
  }
}
