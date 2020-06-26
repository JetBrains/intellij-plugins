// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.model;

import com.intellij.flex.model.bc.JpsFlexModuleOrProjectCompilerOptions;
import com.intellij.flex.model.bc.impl.JpsFlexCompilerOptionsImpl;
import com.intellij.flex.model.bc.impl.JpsFlexCompilerOptionsRole;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;

public final class JpsFlexProjectLevelCompilerOptionsExtension extends JpsCompositeElementBase<JpsFlexProjectLevelCompilerOptionsExtension> {

  private static final JpsElementChildRole<JpsFlexProjectLevelCompilerOptionsExtension> ROLE =
    JpsElementChildRoleBase.create("flex project level compiler options holder");

  private JpsFlexProjectLevelCompilerOptionsExtension() {
    myContainer.setChild(JpsFlexCompilerOptionsRole.INSTANCE);
  }

  private JpsFlexProjectLevelCompilerOptionsExtension(final JpsFlexProjectLevelCompilerOptionsExtension original) {
    super(original);
  }

  @Override
  @NotNull
  public JpsFlexProjectLevelCompilerOptionsExtension createCopy() {
    return new JpsFlexProjectLevelCompilerOptionsExtension(this);
  }

  public static JpsFlexModuleOrProjectCompilerOptions getProjectLevelCompilerOptions(final JpsProject project) {
    final JpsFlexProjectLevelCompilerOptionsExtension child = project.getContainer().getChild(ROLE);
    return child != null ? child.getProjectLevelCompilerOptions()
                         : JpsFlexCompilerOptionsRole.INSTANCE.create();
  }

  private JpsFlexModuleOrProjectCompilerOptions getProjectLevelCompilerOptions() {
    return myContainer.getChild(JpsFlexCompilerOptionsRole.INSTANCE);
  }

  static JpsProjectExtensionSerializer createProjectExtensionSerializer() {
    return new JpsProjectExtensionSerializer("flexCompiler.xml", "FlexIdeProjectLevelCompilerOptionsHolder") {
      @Override
      public void loadExtension(@NotNull final JpsProject project, @NotNull final Element componentTag) {
        JpsFlexProjectLevelCompilerOptionsExtension.loadExtension(project, componentTag);
      }

      @Override
      public void saveExtension(@NotNull final JpsProject project, @NotNull final Element componentTag) {
        JpsFlexProjectLevelCompilerOptionsExtension.saveExtension(project, componentTag);
      }
    };
  }

  /**
   * This is a workaround of the historical bug: in case of *.ipr-project "FlexIdeProjectLevelCompilerOptionsHolder" component is stored in *.iws instead of *.ipr
   */
  static JpsProjectExtensionSerializer createProjectExtensionSerializerIws() {
    return new JpsProjectExtensionSerializer("workspace.xml", "FlexIdeProjectLevelCompilerOptionsHolder") {
      @Override
      public void loadExtension(@NotNull final JpsProject project, @NotNull final Element componentTag) {
        JpsFlexProjectLevelCompilerOptionsExtension.loadExtension(project, componentTag);
      }

      @Override
      public void saveExtension(@NotNull final JpsProject project, @NotNull final Element componentTag) {
        JpsFlexProjectLevelCompilerOptionsExtension.saveExtension(project, componentTag);
      }
    };
  }

  private static void loadExtension(final JpsProject project, final Element componentTag) {
    final JpsFlexProjectLevelCompilerOptionsExtension extension = new JpsFlexProjectLevelCompilerOptionsExtension();
    final JpsFlexCompilerOptionsImpl options = (JpsFlexCompilerOptionsImpl)extension.getProjectLevelCompilerOptions();

    final Attribute annotation = JpsFlexCompilerOptionsImpl.State.class.getAnnotation(Attribute.class);
    final Element compilerOptionsTag = componentTag.getChild(annotation != null ? annotation.value() : "compiler-options");
    if (compilerOptionsTag != null) {
      options.loadState(XmlSerializer.deserialize(compilerOptionsTag, JpsFlexCompilerOptionsImpl.State.class));
    }

    project.getContainer().setChild(ROLE, extension);
  }

  private static void saveExtension(final JpsProject project, final Element componentTag) {
    final JpsFlexProjectLevelCompilerOptionsExtension extension = project.getContainer().getChild(ROLE);
    if (extension != null) {
      final JpsFlexCompilerOptionsImpl compilerOptions = (JpsFlexCompilerOptionsImpl)extension.getProjectLevelCompilerOptions();
      XmlSerializer.serializeInto(compilerOptions.getState(), componentTag);
    }
  }
}
