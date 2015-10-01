package org.jetbrains.plugins.cucumber.completion;

import com.intellij.codeInsight.completion.PrefixMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CucumberPrefixMatcher extends PrefixMatcher {
  private final List<String> prefixWords;

  protected CucumberPrefixMatcher(String prefix) {
    super(prefix);
    prefixWords = getSignificantWords(prefix);
  }

  @Override
  public boolean prefixMatches(@NotNull String name) {
    final List<String> nameWords = getSignificantWords(name);

    int i = 0;
    int j = 0;

    while (i < prefixWords.size()) {
      while (j < nameWords.size() && !nameWords.get(j).contains(prefixWords.get(i))) {
        j++;
      }
      if (j >= nameWords.size()) {
        return false;
      }
      i++;
    }
    return true;
  }

  @NotNull
  @Override
  public PrefixMatcher cloneWithPrefix(@NotNull String prefix) {
    return new CucumberPrefixMatcher(prefix);
  }

  private static boolean isWordToCount(String word) {
    return word.matches(".*[()\\\\#0-9].*");
  }

  private static List<String> getSignificantWords(String source) {
    final List<String> result = new ArrayList<String>();

    final String[] words = source.split(" ");
    for (String word : words) {
      if (isWordToCount(word)) {
        continue;
      }
      result.add(word.toLowerCase());
    }

    return result;
  }
}
