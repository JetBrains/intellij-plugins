// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.jhipster.JdlIconsMapping;
import com.intellij.jhipster.model.JdlDeclarationsModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveState;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.intellij.jhipster.JdlConstants.PREDEFINED_ENTITIES;

final class JdlEntityIdReference extends PsiReferenceBase<JdlId> {
  public JdlEntityIdReference(@NotNull JdlId element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    PsiFile containingFile = myElement.getContainingFile();
    if (containingFile == null) return null;

    List<JdlEntity> entityRefs = new ArrayList<>();

    String myId = myElement.getText(); // todo move ID to mixin
    if (PREDEFINED_ENTITIES.contains(myId)) {
      return new JdlPredefinedSdkEntity(myElement, myId);
    }

    containingFile.processDeclarations((element, state) -> {
      if (element instanceof JdlEntity && Objects.equals(myId, ((JdlEntity)element).getName())) {
        entityRefs.add((JdlEntity)element);
        return false;
      }
      return true;
    }, ResolveState.initial(), null, myElement);

    if (entityRefs.size() != 1) return null;

    return entityRefs.get(0);
  }

  @Override
  public Object @NotNull [] getVariants() {
    PsiFile containingFile = myElement.getContainingFile();
    if (containingFile == null) return ArrayUtilRt.EMPTY_OBJECT_ARRAY;

    Collection<JdlEntity> entities = JdlDeclarationsModel.findAllEntities(containingFile);
    List<LookupElement> items = new ArrayList<>();

    for (JdlEntity entity : entities) {
      items.add(LookupElementBuilder.create(entity)
                  .withIcon(JdlIconsMapping.getEntityIcon()));
    }

    for (String predefinedEntity : PREDEFINED_ENTITIES) {
      items.add(LookupElementBuilder.create(predefinedEntity)
                  .withIcon(JdlIconsMapping.getEntityIcon())
                  .withTypeText("predefined"));
    }

    return items.toArray();
  }
}
