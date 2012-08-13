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

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.codeInsight.PyDynamicMember;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyPsiFacade;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.resolve.QualifiedNameResolver;
import com.jetbrains.python.psi.types.PyModuleMembersProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Supports completion for modules in the 'flask.ext' namespace.
 *
 * @author yole
 */
public class FlaskModuleMembersProvider extends PyModuleMembersProvider {
  @Override
  protected Collection<PyDynamicMember> getMembersByQName(PyFile module, String qName) {
    if (qName.equals("flask.ext")) {
      List<PyDynamicMember> result = new ArrayList<PyDynamicMember>();
      PyPsiFacade psiFacade = PyPsiFacade.getInstance(module.getProject());
      QualifiedNameResolver visitor = psiFacade.qualifiedNameResolver(PyQualifiedName.fromComponents()).fromElement(module);
      List<PsiDirectory> rootElements = visitor.resultsOfType(PsiDirectory.class);
      for (PsiDirectory psiDirectory: rootElements) {
        for (PsiFile file : psiDirectory.getFiles()) {
          String name = FileUtil.getNameWithoutExtension(file.getName());
          if (name.startsWith("flask_")) {
            result.add(new PyDynamicMember(name.substring(6), file));
          }
        }
      }
      visitor = psiFacade.qualifiedNameResolver("flaskext").withPlainDirectories().fromElement(module);
      for (PsiDirectory directory : visitor.resultsOfType(PsiDirectory.class)) {
        for (PsiFile file : directory.getFiles()) {
          if (file instanceof PyFile) {
            String name = FileUtil.getNameWithoutExtension(file.getName());
            result.add(new PyDynamicMember(name, file));
          }
        }
      }
      return result;
    }
    return Collections.emptyList();
  }
}
