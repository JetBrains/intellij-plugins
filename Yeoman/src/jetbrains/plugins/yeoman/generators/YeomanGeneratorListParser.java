package jetbrains.plugins.yeoman.generators;


import com.google.gson.Gson;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class YeomanGeneratorListParser {
  private final Gson gson = new Gson();

  public YeomanGeneratorFullInfo @NotNull [] parse(@Nullable String jsonText) {
    if (StringUtil.isEmpty(jsonText)) return new YeomanGeneratorFullInfo[0];
    final YeomanGeneratorFullInfo[] infos = gson.fromJson(jsonText, YeomanGeneratorFullInfo[].class);
    Arrays.sort(infos);
    return infos;
  }
}
