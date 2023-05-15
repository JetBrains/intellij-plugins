package org.angularjs.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.angularjs.codeInsight.router.AngularJSUiRouterConstants;
import org.jetbrains.annotations.NotNull;

public class AngularUiRouterViewsIndex extends FileBasedIndexExtension<String, AngularNamedItemDefinition> {
  public static final ID<String, AngularNamedItemDefinition> UI_ROUTER_VIEWS_CACHE_INDEX = ID.create("angularjs.ui.router.views.index");
  private final AngularAttributeIndexer myIndexer = new AngularAttributeIndexer(AngularJSUiRouterConstants.uiView);

  @Override
  public @NotNull ID<String, AngularNamedItemDefinition> getName() {
    return UI_ROUTER_VIEWS_CACHE_INDEX;
  }

  @Override
  public @NotNull DataIndexer<String, AngularNamedItemDefinition, FileContent> getIndexer() {
    return myIndexer;
  }

  @Override
  public @NotNull KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @Override
  public @NotNull DataExternalizer<AngularNamedItemDefinition> getValueExternalizer() {
    return AngularViewDefinitionExternalizer.INSTANCE;
  }

  @Override
  public @NotNull FileBasedIndex.InputFilter getInputFilter() {
    return AngularTemplateIndexInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }
}
