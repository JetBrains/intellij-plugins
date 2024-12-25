// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.highlighting;

import com.intellij.codeInspection.reference.EntryPoint;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.configurationStore.XmlSerializer;
import com.intellij.psi.PsiElement;
import com.intellij.util.xmlb.annotations.Transient;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.util.OsgiPsiUtil;

final class OsgiEntryPoint extends EntryPoint {
  public boolean ADD_ACTIVATORS_TO_ENTRIES = true;

  @Override
  public @NotNull String getDisplayName() {
    return OsmorcBundle.message("osgi.activator.entry.point");
  }

  @Override
  public boolean isEntryPoint(@NotNull RefElement refElement, @NotNull PsiElement psiElement) {
    return isEntryPoint(psiElement);
  }

  @Override
  public boolean isEntryPoint(@NotNull PsiElement psiElement) {
    return OsgiPsiUtil.isActivator(psiElement);
  }

  @Transient
  @Override
  public boolean isSelected() {
    return ADD_ACTIVATORS_TO_ENTRIES;
  }

  @Transient
  @Override
  public void setSelected(boolean selected) {
    ADD_ACTIVATORS_TO_ENTRIES = selected;
  }

  @Override
  public void readExternal(Element element) {
    XmlSerializer.deserializeInto(element, this);
  }

  @Override
  public void writeExternal(Element element) {
    XmlSerializer.serializeObjectInto(this, element);
  }
}
