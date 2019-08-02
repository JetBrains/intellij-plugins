package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.filters.position.RootTagFilter;
import com.intellij.psi.filters.position.TargetNamespaceFilter;
import com.intellij.psi.meta.MetaDataContributor;
import com.intellij.psi.meta.MetaDataRegistrar;
import com.intellij.tapestry.core.TapestryConstants;
import org.jetbrains.annotations.NotNull;

public class TapestryMetaDataContributor implements MetaDataContributor {
  @Override
  public void contributeMetaData(@NotNull MetaDataRegistrar registrar) {
    registrar.registerMetaData(new RootTagFilter(new TargetNamespaceFilter(TapestryXmlExtension.tapestryTemplateNamespaces())),
                                                     TapestryNamespaceDescriptor.class);
    registrar.registerMetaData(new RootTagFilter(new TargetNamespaceFilter(TapestryConstants.PARAMETERS_NAMESPACE)),
                                                     TapestryParametersNamespaceDescriptor.class);
  }
}
