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

import com.intellij.struts2.Struts2Icons;
import com.intellij.struts2.dom.struts.action.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Represents {@link Action} element.
 *
 * @author Yann C&eacute;bron
 */
public class ActionNode extends BasicStrutsNode<Action> {

  public ActionNode(@NotNull final Action identifyingElement, @Nullable final String name) {
    super(identifyingElement, name);
  }

  @Override
  @NotNull
  public Icon getIcon() {
    return Struts2Icons.Action;
  }

}
