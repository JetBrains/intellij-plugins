package com.intellij.flex.model;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;

public class JpsFlexCompilerProjectExtension extends JpsElementBase<JpsFlexCompilerProjectExtension> {

  private static final JpsElementChildRole<JpsFlexCompilerProjectExtension> ROLE =
    JpsElementChildRoleBase.create("flex compiler project extension");

  public boolean GENERATE_FLEXMOJOS_CONFIGS = true;

  public boolean USE_BUILT_IN_COMPILER = true;
  public boolean USE_FCSH = false;
  public boolean USE_MXMLC_COMPC = false;
  public boolean PREFER_ASC_20 = true;
  public int MAX_PARALLEL_COMPILATIONS = 4;
  public int HEAP_SIZE_MB = 512;
  public String VM_OPTIONS = "";

  public JpsFlexCompilerProjectExtension() {
  }

  private JpsFlexCompilerProjectExtension(final JpsFlexCompilerProjectExtension original) {
    GENERATE_FLEXMOJOS_CONFIGS = original.GENERATE_FLEXMOJOS_CONFIGS;

    USE_BUILT_IN_COMPILER = original.USE_BUILT_IN_COMPILER;
    USE_FCSH = original.USE_FCSH;
    USE_MXMLC_COMPC = original.USE_MXMLC_COMPC;
    PREFER_ASC_20 = original.PREFER_ASC_20;
    MAX_PARALLEL_COMPILATIONS = original.MAX_PARALLEL_COMPILATIONS;
    HEAP_SIZE_MB = original.HEAP_SIZE_MB;
    VM_OPTIONS = original.VM_OPTIONS;
  }

  @Override
  @NotNull
  public JpsFlexCompilerProjectExtension createCopy() {
    return new JpsFlexCompilerProjectExtension(this);
  }

  @Override
  public void applyChanges(@NotNull final JpsFlexCompilerProjectExtension modified) {
    GENERATE_FLEXMOJOS_CONFIGS = modified.GENERATE_FLEXMOJOS_CONFIGS;

    USE_BUILT_IN_COMPILER = modified.USE_BUILT_IN_COMPILER;
    USE_FCSH = modified.USE_FCSH;
    USE_MXMLC_COMPC = modified.USE_MXMLC_COMPC;
    PREFER_ASC_20 = modified.PREFER_ASC_20;
    MAX_PARALLEL_COMPILATIONS = modified.MAX_PARALLEL_COMPILATIONS;
    HEAP_SIZE_MB = modified.HEAP_SIZE_MB;
    VM_OPTIONS = modified.VM_OPTIONS;
  }

  @NotNull
  public static JpsFlexCompilerProjectExtension getInstance(final JpsProject project) {
    final JpsFlexCompilerProjectExtension child = project.getContainer().getChild(ROLE);
    return child != null ? child : new JpsFlexCompilerProjectExtension();
  }

  static JpsProjectExtensionSerializer createProjectExtensionSerializer() {
    return new JpsProjectExtensionSerializer("flexCompiler.xml", "FlexCompilerConfiguration") {
      @Override
      public void loadExtension(@NotNull final JpsProject project, @NotNull final Element componentTag) {
        JpsFlexCompilerProjectExtension.loadExtension(project, componentTag);
      }
    };
  }

  /**
   * This is a workaround of the historical bug: in case of *.ipr-project "FlexCompilerConfiguration" component is stored in *.iws instead of *.ipr
   */
  static JpsProjectExtensionSerializer createProjectExtensionSerializerIws() {
    return new JpsProjectExtensionSerializer("workspace.xml", "FlexCompilerConfiguration") {
      @Override
      public void loadExtension(@NotNull final JpsProject project, @NotNull final Element componentTag) {
        JpsFlexCompilerProjectExtension.loadExtension(project, componentTag);
      }
    };
  }

  private static void loadExtension(final JpsProject project, final Element componentTag) {
    final JpsFlexCompilerProjectExtension deserialized = XmlSerializer.deserialize(componentTag, JpsFlexCompilerProjectExtension.class);
    project.getContainer().setChild(ROLE, deserialized);
  }
}
