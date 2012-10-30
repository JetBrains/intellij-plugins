package com.intellij.flex.model;

import com.intellij.flex.model.bc.JpsFlexModuleOrProjectCompilerOptions;
import com.intellij.flex.model.bc.impl.JpsFlexCompilerOptionsImpl;
import com.intellij.flex.model.bc.impl.JpsFlexCompilerOptionsRole;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;

public class JpsFlexProjectLevelCompilerOptionsExtension extends JpsCompositeElementBase<JpsFlexProjectLevelCompilerOptionsExtension> {

  private static final JpsElementChildRoleBase<JpsFlexProjectLevelCompilerOptionsExtension> ROLE =
    JpsElementChildRoleBase.create("flex project level compiler options holder");

  private JpsFlexProjectLevelCompilerOptionsExtension() {
    myContainer.setChild(JpsFlexCompilerOptionsRole.INSTANCE);
  }

  private JpsFlexProjectLevelCompilerOptionsExtension(final JpsFlexProjectLevelCompilerOptionsExtension original) {
    super(original);
  }

  @NotNull
  public JpsFlexProjectLevelCompilerOptionsExtension createCopy() {
    return new JpsFlexProjectLevelCompilerOptionsExtension(this);
  }

  public static JpsFlexModuleOrProjectCompilerOptions getProjectLevelCompilerOptions(final JpsProject project) {
    final JpsFlexProjectLevelCompilerOptionsExtension flexProjectLevelCompilerOptionsExtension = project.getContainer().getChild(ROLE);
    return flexProjectLevelCompilerOptionsExtension.getProjectLevelCompilerOptions();
  }

  private JpsFlexModuleOrProjectCompilerOptions getProjectLevelCompilerOptions() {
    return myContainer.getChild(JpsFlexCompilerOptionsRole.INSTANCE);
  }

  public static JpsProjectExtensionSerializer createProjectExtensionSerializer() {
    return new JpsProjectExtensionSerializer("flexCompiler.xml", "FlexIdeProjectLevelCompilerOptionsHolder") {
      public void loadExtension(@NotNull final JpsProject project, @NotNull final Element componentTag) {
        final JpsFlexProjectLevelCompilerOptionsExtension extension = new JpsFlexProjectLevelCompilerOptionsExtension();
        final JpsFlexCompilerOptionsImpl options = (JpsFlexCompilerOptionsImpl)extension.getProjectLevelCompilerOptions();

        final Attribute annotation = JpsFlexCompilerOptionsImpl.State.class.getAnnotation(Attribute.class);
        final Element compilerOptionsTag = componentTag.getChild(annotation != null ? annotation.value() : "compiler-options");
        if (compilerOptionsTag != null) {
          options.loadState(XmlSerializer.deserialize(compilerOptionsTag, JpsFlexCompilerOptionsImpl.State.class));
        }

        project.getContainer().setChild(ROLE, extension);
      }

      public void saveExtension(@NotNull final JpsProject project, @NotNull final Element componentTag) {
        final JpsFlexCompilerOptionsImpl compilerOptions =
          (JpsFlexCompilerOptionsImpl)project.getContainer().getChild(ROLE).getProjectLevelCompilerOptions();
        XmlSerializer.serializeInto(compilerOptions.getState(), componentTag);
      }
    };
  }
}
