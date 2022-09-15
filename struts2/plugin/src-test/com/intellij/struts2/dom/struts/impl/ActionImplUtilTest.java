/*
 * Copyright 2010 The authors
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

package com.intellij.struts2.dom.struts.impl;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ActionImplUtil}.
 *
 * @author Yann C&eacute;bron
 */
public class ActionImplUtilTest {

  @Test
  public void matchesPathExact() {
    assertFalse(ActionImplUtil.matchesPath("myPath", "XyPath"));

    assertTrue(ActionImplUtil.matchesPath("myPath", "myPath"));
    assertTrue(ActionImplUtil.matchesPath("my-Path", "my-Path"));
    assertTrue(ActionImplUtil.matchesPath("my-Path ", "my-Path "));
  }

  @Test
  public void matchesPathWildcard() {

    // single wildcard
    assertTrue(ActionImplUtil.matchesPath("some*", "some"));
    assertTrue(ActionImplUtil.matchesPath("some*", "someX"));
    assertTrue(ActionImplUtil.matchesPath("some*", "someThing"));

    // single '*' wildcard, slash --> not allowed
    assertFalse(ActionImplUtil.matchesPath("some*", "some/"));
    assertFalse(ActionImplUtil.matchesPath("some*", "someT/ing"));
    assertFalse(ActionImplUtil.matchesPath("some*", "some/T/ing"));

    // two wildcards
    assertTrue(ActionImplUtil.matchesPath("some*Action*Stuff", "someActionStuff"));
    assertTrue(ActionImplUtil.matchesPath("some*Action*Stuff", "someXActionXStuff"));
    assertTrue(ActionImplUtil.matchesPath("some*Action*Stuff", "someXYZActionXYZStuff"));

    assertFalse(ActionImplUtil.matchesPath("some*Action*Stuff", "some/Action/Stuff"));
  }

  @Test
  public void matchesPathBangNotation() {

    // simple
    assertTrue(ActionImplUtil.matchesPath("myPath", "myPath!myAction"));

    // wildcard + bang
    assertTrue(ActionImplUtil.matchesPath("myPath*", "myPathSomething!myAction"));
  }

}