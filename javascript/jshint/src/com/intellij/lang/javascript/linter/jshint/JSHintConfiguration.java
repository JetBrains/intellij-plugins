package com.intellij.lang.javascript.linter.jshint;

import com.intellij.javascript.nodejs.util.JSLinterPackage;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.jshint.config.JSHintDescriptor;
import com.intellij.lang.javascript.linter.option.OptionEnumVariant;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
@State(name = "JSHintConfiguration", storages = @Storage("jsLinters/jshint.xml"))
public class JSHintConfiguration extends JSLinterConfiguration<JSHintState> {

  private static final String JSHINT_ELEMENT_NAME = "jshint";
  private static final String IS_CONFIG_FILE_USED_ATTRIBUTE_NAME = "use-config-file";
  private static final String IS_CUSTOM_CONFIG_FILE_USED_ATTRIBUTE_NAME = "use-custom-config-file";
  private static final String CUSTOM_CONFIG_FILE_PATH_ATTRIBUTE_NAME = "custom-config-file-path";
  private static final String OPTION_ELEMENT_NAME = "option";
  private static final JSHintState DEFAULT_STATE = new JSHintState.Builder()
    .setOptionsState(
      new JSHintOptionsState.Builder()
        .put(JSHintOption.FORIN, true)
        .put(JSHintOption.NOARG, true)
        .put(JSHintOption.NOEMPTY, true)
        .put(JSHintOption.EQEQEQ, true)
        .put(JSHintOption.BITWISE, true)
        .put(JSHintOption.STRICT, true)
        .put(JSHintOption.UNDEF, true)
        .put(JSHintOption.CURLY, true)
        .put(JSHintOption.NONEW, true)
        .put(JSHintOption.BROWSER, true)
        .put(JSHintOption.MAXERR, 50)
        .build()
    ).build();

  private final JSLinterPackage myPackage;

  public JSHintConfiguration(@NotNull Project project) {
    super(project);
    myPackage = new JSLinterPackage(project, JSHintDescriptor.PACKAGE_NAME);
  }

  @Override
  protected void savePrivateSettings(@NotNull JSHintState state) {
    myPackage.force(state.getNodePackageRef());
  }

  @Override
  protected @NotNull JSHintState loadPrivateSettings(@NotNull JSHintState state) {
    myPackage.readOrDetect();
    return state.withLinterPackage(myPackage.getPackage());
  }

  public static @NotNull JSHintConfiguration getInstance(@NotNull Project project) {
    return JSLinterConfiguration.getInstance(project, JSHintConfiguration.class);
  }

  @Override
  protected @NotNull Class<? extends JSLinterInspection> getInspectionClass() {
    return JSHintInspection.class;
  }

  @Override
  protected @NotNull Element toXml(@NotNull JSHintState state) {
    Element root = new Element(JSHINT_ELEMENT_NAME);
    root.setAttribute(IS_CONFIG_FILE_USED_ATTRIBUTE_NAME, String.valueOf(state.isConfigFileUsed()));
    if (state.isCustomConfigFileUsed()) {
      root.setAttribute(IS_CUSTOM_CONFIG_FILE_USED_ATTRIBUTE_NAME, Boolean.TRUE.toString());
    }
    String customConfigFilePath = state.getCustomConfigFilePath();
    if (!customConfigFilePath.isEmpty()) {
      root.setAttribute(CUSTOM_CONFIG_FILE_PATH_ATTRIBUTE_NAME, FileUtil.toSystemIndependentName(customConfigFilePath));
    }
    JSHintOptionsState optionsState = state.getOptionsState();
    List<String> optionKeysInOrder = sortOptionKeysInOrder(optionsState.getOptionKeys());
    for (String optionKey : optionKeysInOrder) {
      JSHintOption option = JSHintOption.findByName(optionKey);
      if (option == null) {
        continue;
      }
      Object value = optionsState.getValue(option);
      if (value != null) {
        final String valueStr;
        if (value instanceof OptionEnumVariant variant) {
          valueStr = variant.getValue().toString();
        }
        else {
          valueStr = value.toString();
        }
        Element child = new Element(OPTION_ELEMENT_NAME);
        child.setAttribute(option.getKey(), valueStr);
        root.addContent(child);
      }
    }
    return root;
  }

  private static @NotNull List<String> sortOptionKeysInOrder(@NotNull Collection<String> optionKeys) {
    String[] array = ArrayUtilRt.toStringArray(optionKeys);
    Arrays.sort(array);
    return Arrays.asList(array);
  }

  @Override
  protected @NotNull JSHintState fromXml(@NotNull Element element) {
    JSHintState.Builder builder = new JSHintState.Builder();
    JSHintOptionsState optionsState = loadOptionsValues(element.getChildren());
    builder.setOptionsState(optionsState);
    builder.setConfigFileUsed(Boolean.parseBoolean(element.getAttributeValue(IS_CONFIG_FILE_USED_ATTRIBUTE_NAME)));
    builder.setCustomConfigFileUsed(Boolean.parseBoolean(element.getAttributeValue(IS_CUSTOM_CONFIG_FILE_USED_ATTRIBUTE_NAME)));
    String customConfigFilePath = StringUtil.notNullize(element.getAttributeValue(CUSTOM_CONFIG_FILE_PATH_ATTRIBUTE_NAME));
    builder.setCustomConfigFilePath(FileUtil.toSystemDependentName(customConfigFilePath));
    return builder.build();
  }


  private static @NotNull JSHintOptionsState loadOptionsValues(@NotNull List<Element> optionsElements) {
    JSHintOptionsState.Builder optionsValuesBuilder = new JSHintOptionsState.Builder();
    for (Element child : optionsElements) {
      if (OPTION_ELEMENT_NAME.equals(child.getName())) {
        List<Attribute> attributes = child.getAttributes();
        for (Attribute attribute : attributes) {
          String optionName = attribute.getName();
          JSHintOption option = JSHintOption.findByName(optionName);
          if (option != null) {
            Object value = option.fromString(attribute.getValue());
            if (value != null) {
              optionsValuesBuilder.put(option, value);
            }
          }
        }
      }
    }
    return optionsValuesBuilder.build();
  }

  @Override
  protected @NotNull JSHintState getDefaultState() {
    return DEFAULT_STATE;
  }

}
