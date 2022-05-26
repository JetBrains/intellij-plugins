package jetbrains.plugins.yeoman.generators;


import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

public interface YeomanGeneratorInfo extends Comparable<YeomanGeneratorInfo> {

  @NlsSafe
  String getYoName();

  @NlsSafe
  String getName();

  final class Util {
    public static String getYoName(@Nullable String name) {
      return name == null ? null : StringUtil.replace(name, "generator-", "");
    }

    public static String getName(@Nullable String name) {
      if (name == null) return null;

      if (name.startsWith("generator-")) return name;

      return "generator-" + name;
    }
  }
}
