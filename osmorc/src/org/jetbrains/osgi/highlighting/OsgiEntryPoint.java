/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.jetbrains.osgi.highlighting;

import com.intellij.codeInspection.reference.EntryPoint;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiElement;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.annotations.Transient;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.util.OsgiPsiUtil;

public class OsgiEntryPoint extends EntryPoint {
  public boolean ADD_ACTIVATORS_TO_ENTRIES = true;

  @NotNull
  @Override
  public String getDisplayName() {
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
  public void readExternal(Element element) throws InvalidDataException {
    XmlSerializer.deserializeInto(this, element);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    XmlSerializer.serializeInto(this, element, new SkipDefaultValuesSerializationFilters());
  }
}
