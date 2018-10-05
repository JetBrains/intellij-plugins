/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.dom.struts.strutspackage;

import com.intellij.util.Processor;

import java.util.List;

/**
 * Walks StrutsPackage hierarchically via {@code "extends"}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsPackageHierarchyWalker {

  private final StrutsPackage start;
  private final Processor<? super StrutsPackage> processor;

  public StrutsPackageHierarchyWalker(final StrutsPackage start,
                                      final Processor<? super StrutsPackage> processor) {
    this.start = start;
    this.processor = processor;
  }

  public void walkUp() {
    walkPackage(start);
  }

  private boolean walkPackage(final StrutsPackage startPackage) {
    if (!processor.process(startPackage)) {
      return false;
    }

    final List<StrutsPackage> extendsList = startPackage.getExtends().getValue();
    if (extendsList == null) {
      return true;
    }

    for (StrutsPackage strutsPackage : extendsList) {
      if (!walkPackage(strutsPackage)) {
        return false;
      }
    }
    return true;
  }
}