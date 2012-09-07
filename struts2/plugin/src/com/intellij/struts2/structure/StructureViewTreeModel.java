/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.impl.xml.XmlStructureViewTreeModel;
import com.intellij.ide.util.treeView.smartTree.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.params.Param;
import com.intellij.util.ReflectionCache;
import com.intellij.util.xml.DomElement;
import icons.Struts2Icons;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the sorters, filters etc. available for structure view.
 *
 * @author Yann C&eacute;bron
 */
class StructureViewTreeModel extends XmlStructureViewTreeModel implements StructureViewModel.ElementInfoProvider {

  /**
   * Don't navigate to anything else than XmlTag.
   */
  private static final Class[] MY_CLASSES = new Class[]{XmlTag.class};

  private final DomElement rootElement;

  private final Class[] alwaysPlus;
  private final Class[] alwaysLeaf;

  StructureViewTreeModel(@NotNull final XmlFile xmlFile,
                         @NotNull final DomElement rootElement,
                         @NotNull final Class[] alwaysPlus,
                         @NotNull final Class[] alwaysLeaf) {
    super(xmlFile);
    this.rootElement = rootElement;
    this.alwaysPlus = alwaysPlus;
    this.alwaysLeaf = alwaysLeaf;
  }

  @NotNull
  public com.intellij.ide.structureView.StructureViewTreeElement getRoot() {
    return new StructureViewTreeElement(rootElement);
  }

  @NotNull
  @Override
  protected Class[] getSuitableClasses() {
    return MY_CLASSES;
  }

  @NotNull
  public Sorter[] getSorters() {
    return new Sorter[]{Sorter.ALPHA_SORTER};
  }

  @NotNull
  public Filter[] getFilters() {
    return new Filter[]{new Filter() {
      public boolean isVisible(final TreeElement treeElement) {
        if (!(treeElement instanceof StructureViewTreeElement)) {
          return true;
        }

        return !(((StructureViewTreeElement) treeElement).getElement() instanceof Param);
      }

      public boolean isReverted() {
        return true;
      }

      @NotNull
      public ActionPresentation getPresentation() {
        return new ActionPresentationData(StrutsBundle.message("structure.view.filter.params"),
                                          StrutsBundle.message("structure.view.filter.params"), Struts2Icons.Preferences);
      }

      @NotNull
      public String getName() {
        return StrutsBundle.message("structure.view.filter.params");
      }
    }};
  }


  @Override
  public boolean isAlwaysShowsPlus(final com.intellij.ide.structureView.StructureViewTreeElement element) {
    return isDomElementOfKind(element, alwaysPlus);
  }

  @Override
  public boolean isAlwaysLeaf(final com.intellij.ide.structureView.StructureViewTreeElement element) {
    return isDomElementOfKind(element, alwaysLeaf);
  }

  private static boolean isDomElementOfKind(final com.intellij.ide.structureView.StructureViewTreeElement element,
                                            final Class... kinds) {
    final DomElement domElement = ((StructureViewTreeElement) element).getElement();
    for (final Class clazz : kinds) {
      if (ReflectionCache.isInstance(domElement, clazz)) {
        return true;
      }
    }
    return false;
  }

}
