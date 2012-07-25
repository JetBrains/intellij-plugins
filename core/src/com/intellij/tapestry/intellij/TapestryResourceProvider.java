package com.intellij.tapestry.intellij;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import com.intellij.tapestry.core.TapestryConstants;

/**
 * @author: Fedor.Korotkov
 */
public class TapestryResourceProvider implements StandardResourceProvider {

  @Override
  public void registerResources(ResourceRegistrar registrar) {
    registrar.addIgnoredResource(TapestryConstants.PARAMETERS_NAMESPACE);
  }
}
