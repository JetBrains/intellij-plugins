/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.python;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyReferenceExpression;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Determines the python file and qualified name that is referenced from a python qualified
 * reference expression.
 */
public class PyFileReferenceContext {

  private final QualifiedName fileLocalSymbol;
  private final PyFile file;

  private PyFileReferenceContext(PyFile file, QualifiedName fileLocalSymbol) {
    this.file = file;
    this.fileLocalSymbol = fileLocalSymbol;
  }

  /**
   * Determines if the given element is a reference expression where a part of the qualifiers
   * resolves to a {@link PyFile}. If so, returns a pair of the resolved file and the trailing
   * qualifiers. E.g., given "foo_pb2.Foo.Bar" returns (foo_pb2.py, Foo.Bar)
   *
   * @param pyElement python element under consideration
   * @return resolved file + trailing qualifier if found, otherwise null
   */
  @Nullable
  static PyFileReferenceContext findContext(PsiElement pyElement) {
    PyReferenceExpression referenceExpression =
        PsiTreeUtil.getParentOfType(pyElement, PyReferenceExpression.class);
    List<String> reversedQualifiers = new ArrayList<>();
    while (referenceExpression != null) {
      PsiElement resolved = referenceExpression.getReference().resolve();
      if (resolved instanceof PyFile) {
        return new PyFileReferenceContext(
            (PyFile) resolved, QualifiedName.fromComponents(Lists.reverse(reversedQualifiers)));
      }
      String refName = referenceExpression.getReferencedName();
      if (refName == null) {
        return null;
      }
      reversedQualifiers.add(refName);
      PyExpression qualifier = referenceExpression.getQualifier();
      // If we don't hit a PyFile as the prefix qualifier, just find the file via the resolved
      // element's containing file.
      if (qualifier == null && resolved != null) {
        PsiFile resolvedFile = resolved.getContainingFile();
        if (resolvedFile instanceof PyFile) {
          return new PyFileReferenceContext(
              (PyFile) resolvedFile,
              QualifiedName.fromComponents(Lists.reverse(reversedQualifiers)));
        }
      }
      if (!(qualifier instanceof PyReferenceExpression)) {
        return null;
      }
      referenceExpression = (PyReferenceExpression) qualifier;
    }
    return null;
  }

  PyFile getFile() {
    return file;
  }

  QualifiedName getFileLocalSymbol() {
    return fileLocalSymbol;
  }
}
