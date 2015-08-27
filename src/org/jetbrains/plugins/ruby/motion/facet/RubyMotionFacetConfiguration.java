package org.jetbrains.plugins.ruby.motion.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionFacetConfiguration implements FacetConfiguration {
  @Override
  public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
    return new FacetEditorTab[0];
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {}

  @Override
  public void writeExternal(Element element) throws WriteExternalException {}
}
