/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion.model.files;

import com.intellij.coldFusion.model.psi.*;
import com.intellij.coldFusion.model.psi.impl.CfmlTagScriptImpl;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Factory;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lera Nikolaenko
 * Date: 24.02.2009
 */
public class CfmlFile extends PsiFileBase {
  private final CachedValue<Map<String, CfmlImplicitVariable>> myImplicitVars;
  @NonNls
  public static final String CFMLVARIABLE_MARKER = "@cfmlvariable ";
  @NonNls
  public static final String CFMLJAVALOADER_MARKER = "@javaloader ";

  @NonNls
  public static final Pattern LOADER_DECL_PATTERN_TEMP =
    Pattern.compile("<!---[\\s]*" + CFMLJAVALOADER_MARKER + "[\\s]*name=\"([^\"]+)\".*[\\s]*(loadPaths=\"([^\"]*)\")[\\s]*--->[\\s]*");

  @NonNls
  public static final Pattern IMPLICIT_VAR_DECL_PATTERN_TEMP = Pattern.compile(
    "<!---[\\s]*" + CFMLVARIABLE_MARKER + "[\\s]*name=\"([^\"]+)\"[\\s]*type=\"([^\"]*)\"[\\s]*--->[\\s]*");

  CachedValueProvider<Map<String, CfmlImplicitVariable>> createImplicitVarsProvider() {
    return new CachedValueProvider<Map<String, CfmlImplicitVariable>>() {
      public CachedValueProvider.Result<Map<String, CfmlImplicitVariable>> compute() {
        final Map<String, CfmlImplicitVariable> result = new THashMap<String, CfmlImplicitVariable>();
        CfmlFile.this.accept(new PsiRecursiveElementVisitor() {
          @Override
          public void visitComment(final PsiComment comment) {
            final String text = comment.getText();
            final String[] nameAndType = findVariableNameAndType(text);
            if (nameAndType == null) {
              return;
            }
            CfmlImplicitVariable var = ContainerUtil.getOrCreate(result, nameAndType[0], new Factory<CfmlImplicitVariable>() {
              public CfmlImplicitVariable create() {
                return new CfmlImplicitVariable(CfmlFile.this, comment, nameAndType[0]);
              }
            });
            var.setType(nameAndType[1]);
          }
        });
        return CachedValueProvider.Result.create(result, CfmlFile.this);
      }
    };
  }

  public CfmlFile(FileViewProvider viewProvider, @NotNull Language language) {
    super(viewProvider, language);
    myImplicitVars = CachedValuesManager.getManager(getManager().getProject()).createCachedValue(createImplicitVarsProvider(), false);
  }

  @NotNull
  public FileType getFileType() {
    return CfmlFileType.INSTANCE;
  }

  @NotNull
  public String getPresentableName() {
    return "CfmlFile:" + getName();
  }

  public String toString() {
    return getPresentableName();
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if (!processExportableDeclarations(processor, state)) {
      return false;
    }
    return CfmlPsiUtil.processDeclarations(processor, state, lastParent, this);
  }

  private boolean processExportableDeclarations(PsiScopeProcessor processor, ResolveState state) {
    for (final CfmlImplicitVariable var : myImplicitVars.getValue().values()) {
      if (!processor.execute(var, state)) {
        return false;
      }
    }
    return true;
  }

  @Nullable
  private static String[] findVariableNameAndType(String text) {
    Matcher matcher = IMPLICIT_VAR_DECL_PATTERN_TEMP.matcher(text);
    Matcher javaLoaderMatcher = LOADER_DECL_PATTERN_TEMP.matcher(text);
    if (!matcher.matches()) {
      if (!javaLoaderMatcher.matches()) {
        return null;
      }
      else {
        return new String[]{javaLoaderMatcher.group(1), "javaloader"};
      }
    }
    return new String[]{matcher.group(1), matcher.group(2)};
  }

  @Nullable
  public CfmlImplicitVariable findImplicitVariable(String name) {
    return myImplicitVars.getValue().get(name);
  }

  public Collection<CfmlFunction> getGlobalFunctions() {
    final Collection<CfmlFunction> result = new LinkedList<CfmlFunction>();
    accept(new CfmlRecursiveElementVisitor() {
      @Override
      public void visitCfmlFunction(CfmlFunction function) {
        result.add(function);
      }

      @Override
      public void visitCfmlComponent(CfmlComponent component) {
      }

      @Override
      public void visitCfmlTag(CfmlTag tag) {
        super.visitCfmlTag(tag);    //To change body of overridden methods use File | Settings | File Templates.
      }

      @Override
      public void visitElement(PsiElement element) {
        if (element instanceof CfmlFile || element instanceof CfmlTag) {
          super.visitElement(element);
        }
      }
    });
    return result;
  }

  @Nullable
  public CfmlComponent getComponentDefinition() {
    final Ref<CfmlComponent> ref = new Ref<CfmlComponent>(null);
    accept(new CfmlRecursiveElementVisitor() {
      @Override
      public void visitCfmlFunction(CfmlFunction function) {
      }

      @Override
      public void visitCfmlComponent(CfmlComponent component) {
        ref.set(component);
      }

      @Override
      public void visitCfmlTag(CfmlTag tag) {
        if (tag instanceof CfmlTagScriptImpl) {
          super.visitCfmlTag(tag);
        }
      }

      @Override
      public void visitElement(PsiElement element) {
        if (element instanceof CfmlFile || element instanceof CfmlTagScriptImpl) {
          super.visitElement(element);
        }
      }
    });
    return ref.get();
  }

  public String getComponentQualifiedName(@NotNull String componentName) {
    CfmlImport[] childrenByClass = findChildrenByClass(CfmlImport.class);
    for (CfmlImport importStatement : childrenByClass) {
      if (importStatement.isImported(componentName)) {
        String importString = importStatement.getImportString();
        return importString != null ? importString : "";
      }
    }
    return componentName;
  }

  public Collection<String> getImportStrings() {
    Set<String> result = new HashSet<String>();
    CfmlImport[] childrenByClass = findChildrenByClass(CfmlImport.class);
    for (CfmlImport importStatement : childrenByClass) {
      String importString = importStatement.getImportString();
      if (importString != null) {
        result.add(importString);
      }
    }
    return result;
  }
}
