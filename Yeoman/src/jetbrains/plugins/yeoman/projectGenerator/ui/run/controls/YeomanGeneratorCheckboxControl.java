package jetbrains.plugins.yeoman.projectGenerator.ui.run.controls;


import com.google.gson.*;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.lang.reflect.Type;

import static jetbrains.plugins.yeoman.projectGenerator.ui.run.controls.YeomanGeneratorControlUtil.buildSelectionTitle;
import static jetbrains.plugins.yeoman.projectGenerator.ui.run.controls.YeomanGeneratorControlUtil.buildTitle;

@SuppressWarnings({"MismatchedReadAndWriteOfArray"})
public class YeomanGeneratorCheckboxControl implements YeomanGeneratorControl {

  public static class Choice {
    private @Nls String value;
    private @Nls String name;
    private boolean checked;
  }
  
  private @NonNls String type;
  private @Nls String name;
  private @Nls String message;
  private Choice[] choices;


  public static final Gson GSON = new Gson();

  public static class Adapter implements JsonDeserializer<Choice> {
    @Override
    public Choice deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

      @NlsSafe String value = null;
      @NlsSafe String name = null;
      Boolean checked = null;

      if (json.isJsonObject()) {
        final JsonObject jsonObject = json.getAsJsonObject();
        final JsonElement valueEl = jsonObject.get("value");
        final JsonElement nameEl = jsonObject.get("name");
        final JsonElement checkedEl = jsonObject.get("checked");


        if (null != valueEl && !valueEl.isJsonObject()) {
          if (valueEl.isJsonPrimitive()) {
            value = valueEl.getAsString();
          }
        }
        if (nameEl != null) {
          name = nameEl.getAsString();
        }
        if (checkedEl != null && checkedEl.isJsonPrimitive()) {
          checked = checkedEl.getAsBoolean();
        }
      } else {
        value = json.getAsString();
      }

      final Choice choice = new Choice();
      choice.value = value;
      choice.name = name;
      choice.checked = Boolean.TRUE.equals(checked);
      return choice;
    }
  }

  @Override
  public YeomanGeneratorControlUI createUI() {

    final FormBuilder builder = FormBuilder.createFormBuilder();
    final JBCheckBox[] checkBoxes = new JBCheckBox[choices.length];

    builder.addComponent(buildTitle(StringUtil.isEmpty(message) ? name : message));
    for (int i = 0; i < choices.length; i++) {
      Choice choice = choices[i];

      final JBCheckBox checkBox = new JBCheckBox();
      checkBox.setSelected(choice.checked);

      checkBoxes[i] = checkBox;
      builder.addLabeledComponent(checkBox, buildSelectionTitle(StringUtil.isEmpty(choice.name) ? choice.value : choice.name));
    }


    return new YeomanGeneratorControlUI() {
      @Override
      public JComponent getComponent() {
        return builder.getPanel();
      }

      @Override
      public String getSelectedValue() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < checkBoxes.length; i++) {
          final JBCheckBox checkBox = checkBoxes[i];
          if (checkBox.isSelected()) {
            final int length = builder.length();
            if (length != 0) {
              builder.append(" ");
            }
            builder.append(i);
          }
        }
        return builder.toString();
      }
    };
  }
}
