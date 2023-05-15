package org.angularjs.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

public class AngularAppIndex extends FileBasedIndexExtension<String, AngularNamedItemDefinition> {
  public static final ID<String, AngularNamedItemDefinition> ANGULAR_APP_INDEX = ID.create("angularjs.app.index");
  private final AngularAttributeIndexer myIndexer = new AngularAttributeIndexer("ngApp");

  @Override
  public @NotNull FileBasedIndex.InputFilter getInputFilter() {
    return AngularTemplateIndexInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public @NotNull ID<String, AngularNamedItemDefinition> getName() {
    return ANGULAR_APP_INDEX;
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
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }
}
