package com.intellij.flex.model.sdk;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.library.sdk.JpsSdkType;
import org.jetbrains.jps.model.serialization.library.JpsSdkPropertiesSerializer;

import java.util.ArrayList;
import java.util.Collection;

public class JpsFlexmojosSdkType extends JpsSdkType<JpsSimpleElement<JpsFlexmojosSdkProperties>> {

  public static final String ID = "Flexmojos SDK Type";
  public static final JpsFlexmojosSdkType INSTANCE = new JpsFlexmojosSdkType();

  private static final String COMPILER_CLASSPATH_ELEMENT_NAME = "CompilerClassPath";
  private static final String CLASSPATH_ENTRY_ELEMENT_NAME = "ClassPathEntry";
  private static final String ADL_PATH_ELEMENT_NAME = "AdlPath";
  private static final String AIR_RUNTIME_PATH_ELEMENT_NAME = "AirRuntimePath";

  @Override
  public String getPresentableName() {
    return "Flexmojos SDK";
  }

  public static JpsSdkPropertiesSerializer<JpsSimpleElement<JpsFlexmojosSdkProperties>> createSdkPropertiesSerializer() {
    return new JpsSdkPropertiesSerializer<JpsSimpleElement<JpsFlexmojosSdkProperties>>(ID, INSTANCE) {
      @Override
      @NotNull
      public JpsSimpleElement<JpsFlexmojosSdkProperties> loadProperties(@Nullable final Element propertiesElement) {
        final Collection<String> flexCompilerClasspath = new ArrayList<>();
        String adlPath = "";
        String airRuntimePath = "";

        if (propertiesElement != null) {
          final Element compilerClasspathElement = propertiesElement.getChild(COMPILER_CLASSPATH_ELEMENT_NAME);
          if (compilerClasspathElement != null) {
            for (Element classpathEntryElement : compilerClasspathElement.getChildren(CLASSPATH_ENTRY_ELEMENT_NAME)) {
              flexCompilerClasspath.add(classpathEntryElement.getText());
            }
          }

          adlPath = FileUtil.toSystemIndependentName(StringUtil.notNullize(propertiesElement.getChildText(ADL_PATH_ELEMENT_NAME)));
          airRuntimePath = FileUtil.toSystemIndependentName(
            StringUtil.notNullize(propertiesElement.getChildText(AIR_RUNTIME_PATH_ELEMENT_NAME)));
        }

        final JpsFlexmojosSdkProperties properties = new JpsFlexmojosSdkProperties(flexCompilerClasspath, adlPath, airRuntimePath);
        return JpsElementFactory.getInstance().createSimpleElement(properties);
      }
    };
  }
}
