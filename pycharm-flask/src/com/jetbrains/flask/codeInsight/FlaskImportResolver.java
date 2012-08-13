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
import com.jetbrains.python.psi.PyPsiFacade;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.resolve.QualifiedNameResolveContext;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class FlaskImportResolver implements PyImportResolver {
  private static final PyQualifiedName FLASK_EXT = PyQualifiedName.fromDottedString("flask.ext");

  @Nullable
  @Override
  public PsiElement resolveImportReference(PyQualifiedName qualifiedName,
                                           QualifiedNameResolveContext resolveContext) {
    if (qualifiedName.matchesPrefix(FLASK_EXT) && qualifiedName.getComponentCount() >= 3) {
      PyPsiFacade psiFacade = PyPsiFacade.getInstance(resolveContext.getProject());
      String topName = qualifiedName.getComponents().get(2);
      PyQualifiedName subName = qualifiedName.removeHead(3);
      PyQualifiedName qName = PyQualifiedName.fromComponents("flask_" + topName).append(subName);
      PsiElement item = psiFacade.qualifiedNameResolver(qName).withContext(resolveContext).firstResult();
      if (item != null) {
        return item;
      }
      qName = PyQualifiedName.fromComponents("flaskext", topName).append(subName);
      item = psiFacade.qualifiedNameResolver(qName).withContext(resolveContext).withPlainDirectories().firstResult();
      if (item != null) {
        return item;
      }
    }
    return null;
  }
}
