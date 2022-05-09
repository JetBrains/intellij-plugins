package com.intellij.tapestry.intellij;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import com.intellij.tapestry.core.TapestryConstants;

/**
 * @author Fedor.Korotkov
 */
public class TapestryResourceProvider implements StandardResourceProvider {

  @Override
  public void registerResources(ResourceRegistrar registrar) {
    registrar.addIgnoredResource(TapestryConstants.PARAMETERS_NAMESPACE);
    registrar.addStdResource(TapestryConstants.TEMPLATE_NAMESPACE, "/META-INF/tapestry_5_1_0.xsd", getClass());
    registrar.addStdResource(TapestryConstants.TEMPLATE_NAMESPACE2, "/META-INF/tapestry_5_0_0.xsd", getClass());
    registrar.addStdResource(TapestryConstants.TEMPLATE_NAMESPACE3, "/META-INF/tapestry_5_3.xsd", getClass());
    registrar.addStdResource(TapestryConstants.TEMPLATE_NAMESPACE4, "/META-INF/tapestry_5_4.xsd", getClass());
  }
}
