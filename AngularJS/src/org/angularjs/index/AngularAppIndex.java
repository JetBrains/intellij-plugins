package org.angularjs.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Irina.Chernushina on 3/17/2016.
 */
public class AngularAppIndex extends FileBasedIndexExtension<String, AngularNamedItemDefinition> {
  public static final ID<String, AngularNamedItemDefinition> ANGULAR_APP_INDEX = ID.create("angularjs.app.index");
  private final AngularAttributeIndexer myIndexer = new AngularAttributeIndexer("ng-app");

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return AngularTemplateIndexInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @NotNull
  @Override
  public ID<String, AngularNamedItemDefinition> getName() {
    return ANGULAR_APP_INDEX;
  }

  @NotNull
  @Override
  public DataIndexer<String, AngularNamedItemDefinition, FileContent> getIndexer() {
    return myIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public DataExternalizer<AngularNamedItemDefinition> getValueExternalizer() {
    return AngularViewDefinitionExternalizer.INSTANCE;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }
}
