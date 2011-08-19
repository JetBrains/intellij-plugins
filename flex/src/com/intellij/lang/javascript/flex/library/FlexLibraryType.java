package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryKind;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author nik
 */
public class FlexLibraryType extends LibraryType<DummyLibraryProperties> {
  public static final LibraryKind<DummyLibraryProperties> FLEX_LIBRARY = LibraryKind.create("flex");

  public FlexLibraryType() {
    super(FLEX_LIBRARY);
  }

  @NotNull
  @Override
  public String getCreateActionName() {
    return "ActionScript/Flex";
  }

  @NotNull
  @Override
  public DummyLibraryProperties createDefaultProperties() {
    return DummyLibraryProperties.INSTANCE;
  }

  @Override
  public boolean isSuitableModule(@NotNull Module module, @NotNull FacetsProvider facetsProvider) {
    return ModuleType.get(module).equals(FlexModuleType.getInstance()) || !facetsProvider.getFacetsByType(module, FlexFacet.ID).isEmpty();
  }

  public LibraryPropertiesEditor createPropertiesEditor(@NotNull final LibraryEditorComponent<DummyLibraryProperties> properties) {
    return null;
  }

  @Override
  public Icon getIcon() {
    return PlatformIcons.LIBRARY_ICON;    // TODO: change icon to Flex specific only when automatic library converters are done
  }

  public LibraryRootsComponentDescriptor createLibraryRootsComponentDescriptor() {
    return new FlexLibraryRootsComponentDescriptor();
  }
}
