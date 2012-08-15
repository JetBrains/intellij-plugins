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
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFromImportStatement;
import com.jetbrains.python.psi.PyImportElement;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.resolve.PyCanonicalPathProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class FlaskCanonicalPathProvider implements PyCanonicalPathProvider {
  @Nullable
  @Override
  public PyQualifiedName getCanonicalPath(@NotNull PyQualifiedName qName, PsiElement foothold) {
    if (foothold == null) {
      return null;
    }
    if (flaskReexportsWerkzeug(qName, foothold.getText()) && importsFlask(foothold.getContainingFile())) {
      return PyQualifiedName.fromComponents("flask");
    }
    return null;
  }

  private static boolean flaskReexportsWerkzeug(PyQualifiedName qName, String name) {
    return (qName.toString().equals("werkzeug.exceptions") && FlaskNames.ABORT.equals(name)) ||
           (qName.toString().equals("werkzeug.utils") && FlaskNames.REDIRECT.equals(name));
  }

  private static boolean importsFlask(PsiFile file) {
    if (!(file instanceof PyFile)) {
      return false;
    }
    PyFile pyFile = (PyFile) file;
    PyQualifiedName flask = PyQualifiedName.fromComponents("flask");
    for (PyFromImportStatement importStatement : pyFile.getFromImports()) {
      if (flask.equals(importStatement.getImportSourceQName())) {
        return true;
      }
    }
    for (PyImportElement importElement : pyFile.getImportTargets()) {
      PyQualifiedName qName = importElement.getImportedQName();
      if (qName != null && qName.matchesPrefix(flask)) {
        return true;
      }
    }
    return false;
  }
}
