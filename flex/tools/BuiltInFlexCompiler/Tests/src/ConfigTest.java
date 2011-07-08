import com.intellij.flex.compiler.FlexCompilerUtil;

import java.io.File;

public class ConfigTest {
  public static void main(String[] args) {
    test1();
    test2();
    test3();
  }

  private static void test1() {
    final File[] unexpandedPaths = {
      new File("locales/{locale}"),
      new File("src"),
      new File("en_US/otherLocales/{locale}"),
      new File("src2"),
    };
    final File[] expandedPaths = {
      new File("/absolute/path/locales/en_US"),
      new File("/absolute/path/locales/ja_JP"),
      new File("/absolute/path/locales/ru_RU"),
      new File("/absolute/path/src"),
      new File("/absolute/path/en_US/otherLocales/en_US"),
      new File("/absolute/path/en_US/otherLocales/ja_JP"),
      new File("/absolute/path/en_US/otherLocales/ru_RU"),
      new File("/absolute/path/src2"),
    };
    final String[] locales = {"en_US", "ja_JP", "ru_RU"};

    final String[] expected = {
      "/absolute/path/locales/{locale}",
      "/absolute/path/src",
      "/absolute/path/en_US/otherLocales/{locale}",
      "/absolute/path/src2",
    };

    final File[] result = FlexCompilerUtil.getPathsWithLocaleToken(unexpandedPaths, expandedPaths, locales);

    assert result.length == expected.length;
    for (int i = 0; i < result.length; i++) {
      assert result[i].getPath().equals(expected[i]);
    }
  }

  private static void test2() {
    final File[] unexpandedPaths = {
      new File("en_US/otherLocales/{locale}"),
    };
    final File[] expandedPaths = {
      new File("/absolute/path/en_US/otherLocales/en_US"),
    };
    final String[] locales = {"en_US"};

    final String[] expected = {
      "/absolute/path/{locale}/otherLocales/{locale}",
    };

    final File[] result = FlexCompilerUtil.getPathsWithLocaleToken(unexpandedPaths, expandedPaths, locales);

    assert result.length == expected.length;
    for (int i = 0; i < result.length; i++) {
      assert result[i].getPath().equals(expected[i]);
    }
  }

  private static void test3() {
    final File[] unexpandedPaths = {
      new File("en_US/ja_JP/otherLocales/{locale}"),
    };
    final File[] expandedPaths = {
      new File("/absolute/path/en_US/ja_JP/otherLocales/en_US"),
      new File("/absolute/path/en_US/ja_JP/otherLocales/ja_JP"),
    };
    final String[] locales = {"en_US", "ja_JP"};

    try {
      FlexCompilerUtil.getPathsWithLocaleToken(unexpandedPaths, expandedPaths, locales);
      assert false;
    }
    catch (RuntimeException e) {
      // ok
    }
  }
}
