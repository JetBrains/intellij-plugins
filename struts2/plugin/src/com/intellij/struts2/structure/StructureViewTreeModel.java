package com.intellij.struts2.structure;

import com.intellij.ide.structureView.impl.xml.XmlStructureViewTreeModel;
import com.intellij.ide.util.treeView.smartTree.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.Param;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the sorters, filters etc. available for structure view.
 *
 * @author Yann Cebron
 */
public class StructureViewTreeModel extends XmlStructureViewTreeModel {

  private final DomElement rootElement;

  public StructureViewTreeModel(@NotNull final XmlFile xmlFile,
                                @NotNull final DomElement rootElement) {
    super(xmlFile);
    this.rootElement = rootElement;
  }

  @NotNull
  public com.intellij.ide.structureView.StructureViewTreeElement getRoot() {
    return new StructureViewTreeElement(rootElement);
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
        return new ActionPresentationData("Hide params", "Hide params", StrutsIcons.PARAM);
      }

      @NotNull
      public String getName() {
        return "Hide params";
      }
    }};
  }

}