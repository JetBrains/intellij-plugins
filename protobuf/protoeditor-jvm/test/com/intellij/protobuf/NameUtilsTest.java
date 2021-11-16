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
package com.intellij.protobuf;

import com.intellij.protobuf.jvm.names.NameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for {@link NameUtils}. This is mostly a smoke test. Ultimately, we care about how protoc
 * converts proto names to Java names.
 */
@RunWith(JUnit4.class)
public class NameUtilsTest {

  @Test
  public void underscoreToCamelCase() {
    // Underscores in various spots
    assertThat(NameUtils.underscoreToCamelCase("")).isEmpty();
    assertThat(NameUtils.underscoreToCamelCase("a_b")).isEqualTo("aB");
    assertThat(NameUtils.underscoreToCamelCase("abc_xyz")).isEqualTo("abcXyz");
    assertThat(NameUtils.underscoreToCamelCase("_abc_xyz")).isEqualTo("AbcXyz");
    assertThat(NameUtils.underscoreToCamelCase("abc_xyz_")).isEqualTo("abcXyz");

    // Already capitalized elements (still lower case the first letter)
    assertThat(NameUtils.underscoreToCamelCase("abcXyz")).isEqualTo("abcXyz");
    assertThat(NameUtils.underscoreToCamelCase("Abc_xyz")).isEqualTo("abcXyz");
    assertThat(NameUtils.underscoreToCamelCase("Abc_Xyz")).isEqualTo("abcXyz");

    // Non-underscore separators affect capitalization.
    assertThat(NameUtils.underscoreToCamelCase("abc123xyz")).isEqualTo("abc123Xyz");
    assertThat(NameUtils.underscoreToCamelCase("abc1xyz2")).isEqualTo("abc1Xyz2");
  }

  @Test
  public void underscoreToCapitalizedCamelCase() {
    // Underscores in various spots
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("")).isEmpty();
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("a_b")).isEqualTo("AB");
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("abc_xyz")).isEqualTo("AbcXyz");
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("_abc_xyz")).isEqualTo("AbcXyz");
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("abc_xyz_")).isEqualTo("AbcXyz");

    // Already capitalized elements
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("AbcXyz")).isEqualTo("AbcXyz");
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("Abc_xyz")).isEqualTo("AbcXyz");
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("Abc_Xyz")).isEqualTo("AbcXyz");

    // Non-underscore separators affect capitalization.
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("abc123xyz")).isEqualTo("Abc123Xyz");
    assertThat(NameUtils.underscoreToCapitalizedCamelCase("abc1xyz2")).isEqualTo("Abc1Xyz2");
  }
}
