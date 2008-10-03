/*
 * Copyright 2008 The authors
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
package com.intellij.struts2.graph.beans;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class BasicStrutsEdge {

  private final BasicStrutsNode mySource;
  private final BasicStrutsNode myTarget;

  @NonNls
  @NotNull
  private final String name;

  public BasicStrutsEdge(@NotNull final BasicStrutsNode source,
                         @NotNull final BasicStrutsNode target,
                         @NotNull @NonNls final String name) {
    mySource = source;
    myTarget = target;
    this.name = name;
  }

  @NonNls
  @NotNull
  public String getName() {
    return name;
  }

  public BasicStrutsNode getSource() {
    return mySource;
  }

  public BasicStrutsNode getTarget() {
    return myTarget;
  }

  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final BasicStrutsEdge that = (BasicStrutsEdge) o;

    if (!mySource.equals(that.mySource)) {
      return false;
    }

    if (!myTarget.equals(that.myTarget)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result;
    result = mySource.hashCode();
    result = 31 * result + myTarget.hashCode();
    return result;
  }

}