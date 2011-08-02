/*
 * Copyright 2007 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.struts2.reference;

import org.hamcrest.core.Is;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertThat;

/**
 * @author Yann C&eacute;bron
 */
public class TaglibUtilTest {

  @Test
  public void isDynamicExpression() {
    assertFalse(TaglibUtil.isDynamicExpression(""));

    assertTrue(TaglibUtil.isDynamicExpression("%{any"));
    assertTrue(TaglibUtil.isDynamicExpression("any%{"));
    assertTrue(TaglibUtil.isDynamicExpression("{ 'one', 'two' }"));
  }

  @Test
  public void trimActionPath() {
    assertThat(TaglibUtil.trimActionPath("noBang"), Is.is("noBang"));
    assertThat(TaglibUtil.trimActionPath("noBang!bang"), Is.is("noBang"));
    assertThat(TaglibUtil.trimActionPath("noBang!!bang"), Is.is("noBang"));
  }

}