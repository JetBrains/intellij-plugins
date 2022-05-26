package jetbrains.plugins.yeoman.projectGenerator.ui.run;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.plugins.yeoman.projectGenerator.ui.run.controls.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class YeomanStepFormRenderer {
  public static final Logger LOGGER = Logger.getInstance(YeomanRunGeneratorForm.class);
  public static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(YeomanGeneratorListControl.Choice.class, new YeomanGeneratorListControl.Adapter())
    .registerTypeAdapter(YeomanGeneratorCheckboxControl.Choice.class, new YeomanGeneratorCheckboxControl.Adapter())
    .create();

  public static final String FIELD_TYPE = "type";

  public static final String TYPE_CONFORM = "confirm";
  public static final String TYPE_CHECKBOX = "checkbox";
  public static final String TYPE_EXPAND = "expand";
  public static final String TYPE_LIST = "list";
  public static final String TYPE_RAWLIST = "rawlist";
  public static final String TYPE_INPUT = "input";
  public static final String TYPE_PASSWORD = "password";

  public static final Map<String, Class<? extends YeomanGeneratorControl>> CONTROLS = Map.of(
    TYPE_CONFORM, YeomanGeneratorConformControl.class,
    TYPE_CHECKBOX, YeomanGeneratorCheckboxControl.class,
    TYPE_EXPAND, YeomanGeneratorListControl.class,
    TYPE_LIST, YeomanGeneratorListControl.class,
    TYPE_RAWLIST, YeomanGeneratorListControl.class,
    TYPE_INPUT, YeomanGeneratorInputControl.class,
    TYPE_PASSWORD, YeomanGeneratorInputControl.class
  );

  public YeomanGeneratorControl.YeomanGeneratorControlUI render(@NotNull String json) {
    try {
      final JsonElement parse = new JsonParser().parse(json);
      assert parse.isJsonObject();
      final JsonElement typeElement = parse.getAsJsonObject().get(FIELD_TYPE);
      assert typeElement.isJsonPrimitive();
      final String type = typeElement.getAsString();
      return GSON.fromJson(json, CONTROLS.get(type)).createUI();
    }
    catch (RuntimeException e) {
      LOGGER.error("Cannot parse text " + json, e);
      throw e;
    }
  }
}
