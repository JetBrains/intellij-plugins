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
package com.intellij.flex.uiDesigner.io;

import org.junit.Test;

import static com.intellij.flex.uiDesigner.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;

public class StringRegistryTest {
  @Test
  public void rollback() {
    StringRegistry stringRegistry = new StringRegistry();
    StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(stringRegistry);
    stringWriter.startChange();

    stringWriter.getReference("test");
    stringWriter.getReference("test2");
    stringWriter.getReference("test3");

    stringWriter.commit();

    org.assertj.core.api.Assertions.assertThat(stringRegistry.toArray()).containsExactly("test", "test2", "test3");
    assertThat(stringWriter.size(), equalTo(1));

    stringWriter.startChange();

    stringWriter.getReference("newTest");
    stringWriter.getReference("test2");
    stringWriter.getReference("newTest2");

    stringWriter.rollback();

    org.assertj.core.api.Assertions.assertThat(stringRegistry.toArray()).containsExactly("test", "test2", "test3");
    assertThat(stringWriter.size(), equalTo(1));
  }

  @Test
  public void rollback2() {
    StringRegistry stringRegistry = new StringRegistry();
    StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(stringRegistry);
    stringWriter.startChange();

    stringWriter.getReference("test");
    stringWriter.getReference("test2");
    stringWriter.getReference("test3");

    stringWriter.rollback();

    assertThat(stringRegistry.toArray(), emptyArray());
    assertThat(stringWriter.size(), equalTo(1));
  }
}