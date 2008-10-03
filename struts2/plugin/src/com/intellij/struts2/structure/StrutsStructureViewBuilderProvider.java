package com.intellij.struts2.structure;

import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides structure view for struts.xml files.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsStructureViewBuilderProvider extends BaseStructureViewBuilderProvider {

  @Nullable
  protected DomFileElement getFileElement(@NotNull final XmlFile xmlFile) {
    final DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
    return domManager.getFileElement(xmlFile, StrutsRoot.class);
  }

}