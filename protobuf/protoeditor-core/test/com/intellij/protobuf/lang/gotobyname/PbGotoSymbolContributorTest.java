/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.gotobyname;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Tests for {@link PbGotoSymbolContributor}. */
@TestApplication
public class PbGotoSymbolContributorTest {
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
  public void testGetItemsByName() {
    myFixture.configureByText(
        PbFileType.INSTANCE,
        TestUtils.makeFileWithSyntaxAndPackage(
            "proto2",
            "com.test.google",
            "message SomeMessage {",
            "  message NestedMessage {}",
            "  enum SomeEnum {",
            "    ABC = 0;",
            "    XYZ = 1;",
            "  }",
            "  optional SomeEnum enum_field = 1;",
            "  optional NestedMessage message_field = 2;",
            "  optional AnotherMessage message_field2 = 3;",
            "}",
            "message AnotherMessage {",
            "  optional map<int32, int32> map_field = 1;",
            "  optional group SomeGroup = 2 {}",
            "  oneof pick_oneof {",
            "    optional int32 an_int = 3;",
            "    optional float a_float = 4;",
            "  }",
            "}"));
    PbGotoSymbolContributor contributor = new PbGotoSymbolContributor();
    NavigationItem[] messageResults =
        ReadAction.computeBlocking(() -> contributor.getItemsByName("com.test.google.SomeMessage", "SomeMe", myFixture.getProject(), false));
    assertContainsNames(messageResults, "com.test.google.SomeMessage");
    NavigationItem[] enumResults =
        ReadAction.computeBlocking(() -> contributor.getItemsByName(
            "com.test.google.SomeMessage.SomeEnum", "SomeE", myFixture.getProject(), false));
    assertContainsNames(enumResults, "com.test.google.SomeMessage.SomeEnum");
    NavigationItem[] anotherMessageResults =
        ReadAction.computeBlocking(() -> contributor.getItemsByName(
            "com.test.google.AnotherMessage", "Another", myFixture.getProject(), false));
    assertContainsNames(anotherMessageResults, "com.test.google.AnotherMessage");
    NavigationItem[] groupResults =
        ReadAction.computeBlocking(() -> contributor.getItemsByName(
            "com.test.google.AnotherMessage.SomeGroup", "SomeG", myFixture.getProject(), false));
    assertContainsNames(groupResults, "com.test.google.AnotherMessage.SomeGroup");
    NavigationItem[] oneofResults =
        ReadAction.computeBlocking(() -> contributor.getItemsByName(
            "com.test.google.AnotherMessage.pick_oneof", "pick", myFixture.getProject(), false));
    assertContainsNames(oneofResults, "com.test.google.AnotherMessage.pick_oneof");
  }

  private static void assertContainsNames(NavigationItem[] navigationItems, String... names) {
    List<String> navigationNames = ReadAction.computeBlocking(() ->
        Arrays.stream(navigationItems)
            .map(
                navItem -> {
                  ItemPresentation presentation = navItem.getPresentation();
                  assertNotNull(presentation);
                  assertNotNull(presentation.getLocationString());
                  assertNotNull(presentation.getPresentableText());
                  return String.join(
                      ".", presentation.getLocationString(), presentation.getPresentableText());
                })
            .collect(Collectors.toList()));
    assertThat(navigationNames).containsExactly((Object[])names);
  }
}
