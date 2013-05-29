package com.jetbrains.lang.dart.ide;

import com.intellij.conversion.*;
import com.intellij.javascript.debugger.JSDebuggerBundle;
import com.intellij.javascript.debugger.execution.JavascriptDebugConfigurationType;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.javascript.debugger.execution.JavaScriptDebugConfiguration.JavaScriptDebuggerConfigurationSettings;

public class DartRunConfigurationConverterProvider extends ConverterProvider {
  protected DartRunConfigurationConverterProvider() {
    super("dart-run-configurations-converter");
  }

  @NotNull
  @Override
  public String getConversionDescription() {
    return "Dart run configurations will be converted into JavaScript run configurations";
  }

  @NotNull
  @Override
  public ProjectConverter createConverter(@NotNull ConversionContext context) {
    return new ProjectConverter() {
      @Override
      public ConversionProcessor<RunManagerSettings> createRunConfigurationsConverter() {
        return new ConversionProcessor<RunManagerSettings>() {
          @Override
          public boolean isConversionNeeded(RunManagerSettings settings) {
            for (Element element : settings.getRunConfigurations()) {
              if (DartRunConfigurationConverterProvider.isConversionNeeded(element)) {
                return true;
              }
            }
            return false;
          }

          @Override
          public void process(RunManagerSettings settings) {
            for (Element element : settings.getRunConfigurations()) {
              if (DartRunConfigurationConverterProvider.isConversionNeeded(element)) {
                converter(element);
              }
            }
          }
        };
      }
    };
  }

  public static void converter(Element element) {
    if (!isConversionNeeded(element)) return;
    if ("DartConfigurationType".equalsIgnoreCase(element.getAttributeValue("type"))) {
      convertLocal(element);
    }
    else if ("DartDebugSession".equalsIgnoreCase(element.getAttributeValue("type"))) {
      convertRemote(element);
    }
  }

  private static void convertLocal(Element element) {
    element.setAttribute("type", JavascriptDebugConfigurationType.getTypeInstance().getId());
    element.setAttribute("factoryName", JSDebuggerBundle.message("javascript.debug.configuration.local"));
    element.setAttribute("singleton", "true");
    JavaScriptDebuggerConfigurationSettings settings =
      new JavaScriptDebuggerConfigurationSettings();
    settings.setEngineId("chrome");
    List options = element.getChildren("option");
    for (Object obj : options) {
      assert obj instanceof Element;
      if ("fileUrl".equals(((Element)obj).getAttributeValue("name"))) {
        settings.setFileUrl(((Element)obj).getAttributeValue("value"));
      }
    }
    element.removeContent();
    element.addContent(XmlSerializer.serialize(settings));
  }

  private static void convertRemote(Element element) {
    element.setAttribute("type", JavascriptDebugConfigurationType.getTypeInstance().getId());
    element.setAttribute("factoryName", JSDebuggerBundle.message("javascript.debug.configuration.remote"));
    element.setAttribute("singleton", "true");
    JavaScriptDebuggerConfigurationSettings settings = new JavaScriptDebuggerConfigurationSettings();
    settings.setEngineId("chrome");
    List options = element.getChildren("option");
    Element mappings = null;
    for (Object obj : options) {
      assert obj instanceof Element;
      if ("fileUrl".equals(((Element)obj).getAttributeValue("name"))) {
        settings.setFileUrl(((Element)obj).getAttributeValue("value"));
      }
      else if ("mappings".equals(((Element)obj).getAttributeValue("name"))) {
        mappings = (Element)obj;
      }
    }
    element.removeContent();
    Element remoteSettings = XmlSerializer.serialize(settings);
    element.addContent(remoteSettings);
    if (mappings != null) {
      Element list = mappings.getChild("list");
      if (list != null) {
        remoteSettings.addContent(list.removeContent());
      }
    }
  }

  public static boolean isConversionNeeded(Element element) {
    return "DartConfigurationType".equalsIgnoreCase(element.getAttributeValue("type")) ||
           "DartDebugSession".equalsIgnoreCase(element.getAttributeValue("type"));
  }
}
