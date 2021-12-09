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
package com.intellij.protobuf.ide.views;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.breadcrumbs.BreadcrumbsItem;
import com.intellij.protobuf.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Tests for {@link PbBreadcrumbsInfoProvider}. */
public class PbBreadcrumbsTest extends PbCodeInsightFixtureTestCase {

  private static final String EXPECTED =
      String.join(
          "\n",
          "[]",
          "[Message1;message <b>test.Message1</b>]",
          "[Message1;message <b>test.Message1</b>][field1;field <b>test.Message1.field1</b>]",
          "[Message1;message <b>test.Message1</b>]"
              + "[SubMessage;message <b>test.Message1.SubMessage</b>]"
              + "[SubSubMessage;message <b>test.Message1.SubMessage.SubSubMessage</b>]",
          "[Message1;message <b>test.Message1</b>][map_field;map <b>test.Message1.map_field</b>]",
          "[Message1;message <b>test.Message1</b>][Enum1;enum <b>test.Message1.Enum1</b>]",
          "[Message1;message <b>test.Message1</b>][Enum1;enum <b>test.Message1.Enum1</b>]"
              + "[FOO;enum value <b>test.Message1.FOO</b>]",
          "[Message1;message <b>test.Message1</b>]"
              + "[GroupField;group <b>test.Message1.GroupField</b>]",
          "[Message1;message <b>test.Message1</b>][Oneof1;oneof <b>test.Message1.Oneof1</b>]",
          "[Message1;message <b>test.Message1</b>][Oneof1;oneof <b>test.Message1.Oneof1</b>]"
              + "[GroupInOneof;group <b>test.Message1.GroupInOneof</b>]",
          "[Service1;service <b>test.Service1</b>]",
          "[Service1;service <b>test.Service1</b>][Foo;rpc <b>test.Service1.Foo</b>]");

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.registerTestdataFileExtension();
  }

  @Override
  public String getTestDataPath() {
    return super.getTestDataPath() + "/ide/views/";
  }

  public void testAll() {
    myFixture.configureByFile("BreadcrumbsTest.proto.testdata");
    final CaretModel caretModel = myFixture.getEditor().getCaretModel();

    List<String> outputs = new ArrayList<>();
    List<Integer> offsets =
        caretModel.getAllCarets().stream().map(Caret::getOffset).collect(Collectors.toList());
    for (int offset : offsets) {
      caretModel.moveToOffset(offset);
      List<BreadcrumbsItem> breadcrumbs = getBreadcrumbs(myFixture);
      if (breadcrumbs.isEmpty()) {
        outputs.add("[]");
      } else {
        outputs.add(
            breadcrumbs.stream().map(BreadcrumbsItem::toString).collect(Collectors.joining()));
      }
    }

    assertSameLines(EXPECTED, String.join("\n", outputs));
  }

  @NotNull
  private static List<BreadcrumbsItem> getBreadcrumbs(@NotNull CodeInsightTestFixture fixture) {
    PsiElement element = fixture.getFile().findElementAt(fixture.getCaretOffset());
    if (element == null) {
      return Collections.emptyList();
    }
    final Language language = element.getContainingFile().getLanguage();

    final BreadcrumbsProvider provider =
        ContainerUtil.find(
            BreadcrumbsProvider.EP_NAME.getExtensions(),
            p -> Arrays.asList(p.getLanguages()).contains(language));
    if (provider == null) {
      return Collections.emptyList();
    }

    List<BreadcrumbsItem> result = new ArrayList<>();
    while (element != null) {
      if (provider.acceptElement(element)) {
        result.add(
            new MockBreadcrumbsItem(
                provider.getElementInfo(element), provider.getElementTooltip(element)));
      }
      element = provider.getParent(element);
    }
    Collections.reverse(result);
    return result;
  }

  private static class MockBreadcrumbsItem extends BreadcrumbsItem {
    @Nullable private final String myDisplayText;
    @Nullable private final String myTooltip;

    private MockBreadcrumbsItem(@Nullable String displayText, @Nullable String tooltip) {
      myDisplayText = displayText;
      myTooltip = tooltip;
    }

    @Nullable
    @Override
    public String getDisplayText() {
      return myDisplayText;
    }

    @Nullable
    @Override
    public String getTooltip() {
      return myTooltip;
    }

    @Override
    public String toString() {
      return "[" + getDisplayText() + ";" + getTooltip() + "]";
    }
  }
}
