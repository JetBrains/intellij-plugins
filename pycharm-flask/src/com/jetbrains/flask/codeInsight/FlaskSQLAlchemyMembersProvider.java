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

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.BaseScopeProcessor;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.codeInsight.PyDynamicMember;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyImportElement;
import com.jetbrains.python.psi.PyPsiFacade;
import com.jetbrains.python.psi.types.PyClassMembersProvider;
import com.jetbrains.python.psi.types.PyClassType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Supports the dynamic adding of members in the SQLAlchemy class of Flask-SQLAlchemy.
 *
 * @author yole
 */
public class FlaskSQLAlchemyMembersProvider implements PyClassMembersProvider {

  public static final String FLASK_SQLALCHEMY = "flask_sqlalchemy.SQLAlchemy";

  @Override
  public Collection<PyDynamicMember> getMembers(PyClassType clazz) {
    if (FLASK_SQLALCHEMY.equals(clazz.getClassQName())) {
      final List<PyDynamicMember> result = new ArrayList<PyDynamicMember>();
      PyClass cls = clazz.getPyClass();
      collectModuleMembers(cls, "sqlalchemy", result);
      collectModuleMembers(cls, "sqlalchemy.orm", result);
      return result;
    }
    return Collections.emptyList();
  }

  @Override
  public PsiElement resolveMember(PyClassType clazz, String name) {
    if (FLASK_SQLALCHEMY.equals(clazz.getClassQName())) {
      PyFile sqlalchemy = findModule(clazz.getPyClass(), "sqlalchemy");
      if (sqlalchemy != null) {
        PsiElement result = sqlalchemy.getElementNamed(name);
        if (result != null) return result;
      }
      sqlalchemy = findModule(clazz.getPyClass(), "sqlalchemy.orm");
      if (sqlalchemy != null) {
        PsiElement result = sqlalchemy.getElementNamed(name);
        if (result != null) return result;
      }
    }
    return null;
  }

  private static void collectModuleMembers(PyClass cls, String moduleName, final List<PyDynamicMember> result) {
    PyFile module = findModule(cls, moduleName);
    if (module != null) {
      module.processDeclarations(new BaseScopeProcessor() {
        @Override
        public boolean execute(@NotNull PsiElement element, ResolveState state) {
          if (element instanceof PyImportElement) {
            element = ((PyImportElement) element).resolve();
          }
          if (element instanceof PsiNamedElement) {
            String name = ((PsiNamedElement)element).getName();
            if (name != null) {
              result.add(new PyDynamicMember(name, element));
            }
          }
          return true;
        }
      }, ResolveState.initial(), null, module);
    }
  }

  @Nullable
  private static PyFile findModule(PyClass cls, String moduleName) {
    PsiElement module = PyPsiFacade.getInstance(cls.getProject()).qualifiedNameResolver(moduleName).fromElement(cls).firstResult();
    if (module instanceof PsiDirectory) {
      module = ((PsiDirectory) module).findFile(PyNames.INIT_DOT_PY);
    }
    return module instanceof PyFile ? (PyFile) module : null;
  }
}
