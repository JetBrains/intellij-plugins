// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.jhipster.JdlConstants;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveState;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.intellij.psi.search.GlobalSearchScope.allScope;

public final class JdlFieldTypeReference extends PsiReferenceBase<JdlFieldType> {
  public JdlFieldTypeReference(@NotNull JdlFieldType element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    String typeName = myElement.getTypeName();
    String jvmTypeName = JdlConstants.FIELD_TYPES.get(typeName);

    if (jvmTypeName != null) {
      Project project = myElement.getProject();
      return JavaPsiFacade.getInstance(project).findClass(jvmTypeName, allScope(project));
    }

    PsiFile containingFile = myElement.getContainingFile();
    if (containingFile == null) return null;

    List<JdlEnum> enumRefs = new ArrayList<>();

    // todo support files in the same directory
    containingFile.processDeclarations((element, state) -> {
      if (element instanceof JdlEnum && Objects.equals(typeName, ((JdlEnum)element).getName())) {
        enumRefs.add((JdlEnum)element);
        return false;
      }
      return true;
    }, ResolveState.initial(), null, myElement);

    if (enumRefs.size() != 1) return null;

    return enumRefs.get(0);
  }

  @Override
  public Object @NotNull [] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY; // provided by completion contributor
  }
}