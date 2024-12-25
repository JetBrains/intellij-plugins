// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.files;

import com.intellij.coldFusion.model.psi.*;
import com.intellij.coldFusion.model.psi.impl.CfmlTagScriptImpl;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CfmlFile extends PsiFileBase {
  private final CachedValue<Map<String, CfmlImplicitVariable>> myImplicitVars;
  public static final @NonNls String CFMLVARIABLE_MARKER = "@cfmlvariable ";
  public static final @NonNls String CFMLJAVALOADER_MARKER = "@javaloader ";

  public static final @NonNls Pattern LOADER_DECL_PATTERN_TEMP =
    Pattern.compile("<!---[\\s]*" + CFMLJAVALOADER_MARKER + "[\\s]*name=\"([^\"]+)\".*[\\s]*(loadPaths=\"([^\"]*)\")[\\s]*--->[\\s]*");

  public static final @NonNls Pattern IMPLICIT_VAR_DECL_PATTERN_TEMP = Pattern.compile(
    "<!---[\\s]*" + CFMLVARIABLE_MARKER + "[\\s]*name=\"([^\"]+)\"[\\s]*type=\"([^\"]*)\"[\\s]*--->[\\s]*");

  CachedValueProvider<Map<String, CfmlImplicitVariable>> createImplicitVarsProvider() {
    return () -> {
      final Map<String, CfmlImplicitVariable> result = new HashMap<>();
      this.accept(new PsiRecursiveElementVisitor() {
        @Override
        public void visitComment(final @NotNull PsiComment comment) {
          final String text = comment.getText();
          final String[] nameAndType = findVariableNameAndType(text);
          if (nameAndType == null) {
            return;
          }
          CfmlImplicitVariable var = result.computeIfAbsent(nameAndType[0], __ -> {
            return new CfmlImplicitVariable(CfmlFile.this, comment, nameAndType[0]);
          });
          var.setType(nameAndType[1]);
        }
      });
      return CachedValueProvider.Result.create(result, this);
    };
  }

  public CfmlFile(FileViewProvider viewProvider, @NotNull Language language) {
    super(viewProvider, language);
    myImplicitVars = CachedValuesManager.getManager(getManager().getProject()).createCachedValue(createImplicitVarsProvider(), false);
  }

  @Override
  public @NotNull FileType getFileType() {
    return CfmlFileType.INSTANCE;
  }

  public @NotNull String getPresentableName() {
    return "CfmlFile:" + getName();
  }

  @Override
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

  private static String @Nullable [] findVariableNameAndType(String text) {
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

  public @Nullable CfmlImplicitVariable findImplicitVariable(String name) {
    return myImplicitVars.getValue().get(name);
  }

  public Collection<CfmlFunction> getGlobalFunctions() {
    final Collection<CfmlFunction> result = new LinkedList<>();
    accept(new CfmlRecursiveElementVisitor() {
      @Override
      public void visitCfmlFunction(CfmlFunction function) {
        result.add(function);
      }

      @Override
      public void visitCfmlComponent(CfmlComponent component) {
      }

      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (element instanceof CfmlFile || element instanceof CfmlTag) {
          super.visitElement(element);
        }
      }
    });
    return result;
  }

  public @Nullable CfmlComponent getComponentDefinition() {
    final Ref<CfmlComponent> ref = new Ref<>(null);
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
      public void visitElement(@NotNull PsiElement element) {
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
    Set<String> result = new HashSet<>();
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
