package jetbrains.plugins.yeoman.projectGenerator.ui.run.controls;


import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.Type;

import static jetbrains.plugins.yeoman.projectGenerator.ui.run.controls.YeomanGeneratorControlUtil.buildTitle;
import static jetbrains.plugins.yeoman.projectGenerator.ui.run.controls.YeomanGeneratorControlUtil.wrapText;

@SuppressWarnings({"unused", "MismatchedReadAndWriteOfArray"})
public class YeomanGeneratorListControl implements YeomanGeneratorControl {

  public static final String DIFF_VALUE = "diff";


  public static class Choice {
    @Nullable @Nls
    private String value;
    @Nullable
    private String key;
    @Nullable @Nls
    private String name;
  }

  public static final Gson GSON = new Gson();

  public static class Adapter implements JsonDeserializer<Choice> {
    @Override
    public Choice deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

      @NlsSafe String value = null;
      @NlsSafe String name = null;
      String key = null;

      if (json.isJsonObject()) {
        final JsonObject jsonObject = json.getAsJsonObject();
        final JsonElement valueEl = jsonObject.get("value");
        final JsonElement nameEl = jsonObject.get("name");
        final JsonElement keyEl = jsonObject.get("key");


        if (null != valueEl && !valueEl.isJsonObject()) {
          if (valueEl.isJsonPrimitive()) {
            value = valueEl.getAsString();
          }
        }
        if (nameEl != null) {
          name = nameEl.getAsString();
        }
        if (keyEl != null) {
          key = keyEl.getAsString();
        }
      }
      else {
        value = json.getAsString();
      }

      final Choice choice = new Choice();
      choice.value = value;
      choice.name = name;
      choice.key = key;
      return choice;
    }
  }


  @NonNls
  private String type;
  @Nls
  private String name;
  @Nls
  private String message;
  private Choice[] choices;

  @SerializedName("default")
  private final Object defaultValueObject = "";


  private int getDefaultValueNumber() {
    if (defaultValueObject instanceof Number) {
      return ((Number)defaultValueObject).intValue();
    }

    if (defaultValueObject instanceof String) {
      for (int i = 0; i < choices.length; i++) {
        final Choice choice = choices[i];

        if (defaultValueObject.equals(choice.value)) {
          return i;
        }
      }
    }

    return -1;
  }

  @Override
  public YeomanGeneratorControlUI createUI() {
    final FormBuilder builder = FormBuilder.createFormBuilder();
    builder.addComponent(buildTitle(StringUtil.isEmpty(message) ? name : message));

    final ButtonGroup group = new ButtonGroup();
    final JBRadioButton[] buttons = new JBRadioButton[choices.length];

    // don't show 'diff'
    int diffButtonIndex = -1;

    for (int i = 0; i < choices.length; i++) {
      final Choice choice = choices[i];
      final JBRadioButton button = new JBRadioButton(getRadioText(choice));

      buttons[i] = button;
      if (isDiffChoice(choice)) {
        diffButtonIndex = i;
      }
      else {
        builder.addComponent(button);
        group.add(button);
      }
    }

    final int defaultValue = getDefaultValueNumber();
    if (defaultValue != -1 && diffButtonIndex != defaultValue) {
      buttons[defaultValue].setSelected(true);
    }
    else {
      buttons[0].setSelected(true);
    }

    return new YeomanGeneratorControlUI() {
      @Override
      public JComponent getComponent() {
        return builder.getPanel();
      }

      @Override
      public String getSelectedValue() {
        for (int i = 0; i < buttons.length; i++) {
          final JBRadioButton button = buttons[i];
          if (button.isSelected()) return String.valueOf(i);
        }
        return "";
      }
    };
  }

  @NotNull
  public @Nls String getRadioText(@NotNull Choice choice) {
    return wrapText(StringUtil.isEmpty(choice.name) ? choice.value == null ? "" : choice.value : choice.name);
  }

  private static boolean isDiffChoice(@NotNull Choice choice) {
    if (!StringUtil.isEmpty(choice.value)) {
      return DIFF_VALUE.equals(choice.value);
    }

    return "d".equals(choice.key) && StringUtil.toLowerCase(StringUtil.notNullize(choice.name)).startsWith("show the differences");
  }
}
