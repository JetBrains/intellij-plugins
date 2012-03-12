package com.google.jstestdriver.idea.execution.settings;

import com.intellij.util.ObjectUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.google.jstestdriver.idea.util.EnumUtils;
import com.intellij.openapi.util.JDOMExternalizer;

public class JstdRunSettingsSerializationUtils {

  private enum Key {
    TEST_TYPE("configLocationType"),
    JSTD_CONFIG_FILE("settingsFile"),
    ALL_IN_DIRECTORY("allInDirectory"),
    CONFIG_TYPE("configType"),
    JS_FILE("jsFile"),
    TEST_CASE("testCase"),
    TEST_METHOD("testMethod"),
    SERVER_ADDRESS("serverAddress"),
    SERVER_TYPE("serverType");

    private final String key;

    Key(String key) {
      this.key = key;
    }

    public String getKey() {
      return key;
    }
  }

  private JstdRunSettingsSerializationUtils() {}

  public static JstdRunSettings readFromJDomElement(Element element) {
    JstdRunSettings.Builder builder = new JstdRunSettings.Builder();

    TestType testType = readEnumByName(element, Key.TEST_TYPE, TestType.CONFIG_FILE);
    builder.setTestType(testType);
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      String directory = readString(element, Key.ALL_IN_DIRECTORY, "");
      builder.setDirectory(directory);
    } else if (testType == TestType.CONFIG_FILE) {
      String configFile = readString(element, Key.JSTD_CONFIG_FILE, "");
      builder.setConfigFile(configFile);
    } else if (testType == TestType.JS_FILE) {
      readJsFile(builder, element);
    } else if (testType == TestType.TEST_CASE) {
      readTestCase(builder, element);
    } else if (testType == TestType.TEST_METHOD) {
      readTestMethod(builder, element);
    }
    ServerType serverType = readEnumByName(element, Key.SERVER_TYPE, ServerType.INTERNAL);
    builder.setServerType(serverType);
    if (serverType == ServerType.EXTERNAL) {
      String serverAddress = readString(element, Key.SERVER_ADDRESS, "");
      builder.setServerAddress(serverAddress);
    }
    return builder.build();
  }

  private static void readTestMethod(JstdRunSettings.Builder builder, Element element) {
    readTestCase(builder, element);
    String testMethodName = readString(element, Key.TEST_METHOD, "");
    builder.setTestMethodName(testMethodName);
  }

  private static void readTestCase(JstdRunSettings.Builder builder, Element element) {
    readJsFile(builder, element);
    String testCaseName = readString(element, Key.TEST_CASE, "");
    builder.setTestCaseName(testCaseName);
  }

  private static void readJsFile(JstdRunSettings.Builder builder, Element element) {
    JstdConfigType configType = readEnumByName(element, Key.CONFIG_TYPE, JstdConfigType.GENERATED);
    builder.setConfigType(configType);
    if (configType == JstdConfigType.FILE_PATH) {
      String configFile = readString(element, Key.JSTD_CONFIG_FILE, "");
      builder.setConfigFile(configFile);
    }
    String jsFile = readString(element, Key.JS_FILE, "");
    builder.setJSFilePath(jsFile);
  }

  public static void writeFromJDomElement(Element element, JstdRunSettings runSettings) {
    TestType testType = runSettings.getTestType();
    writeString(element, Key.TEST_TYPE, testType.name());
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      writeString(element, Key.ALL_IN_DIRECTORY, runSettings.getDirectory());
    } else if (testType == TestType.CONFIG_FILE) {
      writeString(element, Key.JSTD_CONFIG_FILE, runSettings.getConfigFile());
    } else if (testType == TestType.JS_FILE) {
      writeJsFile(element, runSettings);
    } else if (testType == TestType.TEST_CASE) {
      writeTestCase(runSettings, element);
    } else if (testType == TestType.TEST_METHOD) {
      writeTestMethod(runSettings, element);
    }
    writeString(element, Key.SERVER_TYPE, runSettings.getServerType().name());
    if (runSettings.getServerType() == ServerType.EXTERNAL) {
      writeString(element, Key.SERVER_ADDRESS, runSettings.getServerAddress());
    }
  }

  private static void writeTestMethod(JstdRunSettings runSettings, Element element) {
    writeTestCase(runSettings, element);
    writeString(element, Key.TEST_METHOD, runSettings.getTestMethodName());
  }

  private static void writeTestCase(JstdRunSettings runSettings, Element element) {
    writeJsFile(element, runSettings);
    writeString(element, Key.TEST_CASE, runSettings.getTestCaseName());
  }

  private static void writeJsFile(Element element, JstdRunSettings runSettings) {
    JstdConfigType configType = runSettings.getConfigType();
    writeString(element, Key.CONFIG_TYPE, configType.name());
    if (configType == JstdConfigType.FILE_PATH) {
      writeString(element, Key.JSTD_CONFIG_FILE, runSettings.getConfigFile());
    }
    writeString(element, Key.JS_FILE, runSettings.getJsFilePath());
  }

  @SuppressWarnings({"unchecked"})
  @NotNull
  private static <E extends Enum<E>> E readEnumByName(Element element, Key key, @NotNull E defaultValue) {
    String str = readString(element, key, "");
    E enumConstant = EnumUtils.findEnum(defaultValue.getDeclaringClass(), str);
    return ObjectUtils.notNull(enumConstant, defaultValue);
  }

  private static void writeString(Element element, Key key, String value) {
    JDOMExternalizer.write(element, key.getKey(), value);
  }

  @NotNull
  private static String readString(Element element, Key key, @NotNull String defaultValue) {
    String value = JDOMExternalizer.readString(element, key.getKey());
    return value != null ? value : defaultValue;
  }

}
