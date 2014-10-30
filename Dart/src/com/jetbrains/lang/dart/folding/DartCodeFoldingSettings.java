package com.jetbrains.lang.dart.folding;

import com.intellij.openapi.components.ServiceManager;

public abstract class DartCodeFoldingSettings {

<<<<<<< HEAD
  @SuppressWarnings({"WeakerAccess"}) public boolean COLLAPSE_GENERIC_PARAMETERS = false;
=======
@State(
  name = "DartCodeFoldingSettings",
  storages = {
    @Storage(
      file = StoragePathMacros.APP_CONFIG + "/editor.codeinsight.xml"
    )}
)
public class DartCodeFoldingSettings implements PersistentStateComponent<DartCodeFoldingSettings>, ExportableComponent {
  private boolean myCollapseGenericParams = false;
>>>>>>> 0531d5f... Optimization and fixes for Dart generics folding

  public static DartCodeFoldingSettings getInstance() {
    return ServiceManager.getService(DartCodeFoldingSettings.class);
  }

  public abstract boolean isCollapseGenericParameters();

<<<<<<< HEAD
  public abstract void setCollapseGenericParameters(boolean value);
=======
  // property name must be equal to checkBox() argument in DartCodeFoldingOptionsProvider
  public boolean isCollapseGenericParameters() {
    return myCollapseGenericParams;
  }

  public void setCollapseGenericParameters(final boolean collapseGenericParams) {
    myCollapseGenericParams = collapseGenericParams;
  }
>>>>>>> 0531d5f... Optimization and fixes for Dart generics folding
}
