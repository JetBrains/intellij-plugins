/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
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
package com.jetbrains.flask.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyPsiFacade;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class FlaskImportResolver implements PyImportResolver {
  private static final PyQualifiedName FLASK_EXT = PyQualifiedName.fromDottedString("flask.ext");

  @Nullable
  @Override
  public PsiElement resolveImportReference(@NotNull PyElement importElement,
                                           @NotNull PyQualifiedName importText,
                                           @Nullable PyQualifiedName importFrom) {
    if (importFrom != null && importFrom.matchesPrefix(FLASK_EXT)) {
      return resolveFlaskExtModule(importElement, importText.getComponents().get(0));
    }
    else if (importText.matchesPrefix(FLASK_EXT) && importText.getComponentCount() >= 3) {
      return resolveFlaskExtModule(importElement, importText.getComponents().get(2));
    }
    return null;
  }

  @Nullable
  private static PsiElement resolveFlaskExtModule(PyElement importElement, String submodule) {
    PyPsiFacade psiFacade = PyPsiFacade.getInstance(importElement.getProject());
    PsiFileSystemItem item = psiFacade.qualifiedNameResolver("flask_" + submodule).fromElement(importElement).firstResult();
    if (item != null) {
      return item;
    }
    item = psiFacade.qualifiedNameResolver("flaskext." + submodule).fromElement(importElement).firstResult();
    if (item != null) {
      return item;
    }
    return null;
  }
}
