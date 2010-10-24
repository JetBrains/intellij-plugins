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
 * Tests for {@link ActionUtil}.
 *
 * @author Yann C&eacute;bron
 */
public class ActionUtilTest {

  @Test
  public void matchesPathExact() {
    assertFalse(ActionUtil.matchesPath("myPath", "XyPath"));

    assertTrue(ActionUtil.matchesPath("myPath", "myPath"));
    assertTrue(ActionUtil.matchesPath("my-Path", "my-Path"));
    assertTrue(ActionUtil.matchesPath("my-Path ", "my-Path "));
  }

  @Test
  public void matchesPathWildcard() {

    // single wildcard
    assertTrue(ActionUtil.matchesPath("some*", "some"));
    assertTrue(ActionUtil.matchesPath("some*", "someX"));
    assertTrue(ActionUtil.matchesPath("some*", "someThing"));

    // single '*' wildcard, slash --> not allowed
    assertFalse(ActionUtil.matchesPath("some*", "some/"));
    assertFalse(ActionUtil.matchesPath("some*", "someT/ing"));
    assertFalse(ActionUtil.matchesPath("some*", "some/T/ing"));

    // two wildcards
    assertTrue(ActionUtil.matchesPath("some*Action*Stuff", "someActionStuff"));
    assertTrue(ActionUtil.matchesPath("some*Action*Stuff", "someXActionXStuff"));
    assertTrue(ActionUtil.matchesPath("some*Action*Stuff", "someXYZActionXYZStuff"));

    assertFalse(ActionUtil.matchesPath("some*Action*Stuff", "some/Action/Stuff"));
  }

  @Test
  public void matchesPathBangNotation() {

    // simple
    assertTrue(ActionUtil.matchesPath("myPath", "myPath!myAction"));

    // wildcard + bang
    assertTrue(ActionUtil.matchesPath("myPath*", "myPathSomething!myAction"));
  }

}