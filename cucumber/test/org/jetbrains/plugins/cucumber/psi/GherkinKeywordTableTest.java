package org.jetbrains.plugins.cucumber.psi;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GherkinKeywordTableTest {
  private final GherkinKeywordTable keywordTable = new GherkinKeywordTable();

  @Test
  public void initiallyTheTableDoesNotContainAKeyword() throws Exception {
    assertThat(keywordTable.tableContainsKeyword(GherkinTokenTypes.FEATURE_KEYWORD, "keyword"), is(false));
  }

  @Test
  public void giveInformationAboutContainedKeys() throws Exception {
    keywordTable.put(GherkinTokenTypes.FEATURE_KEYWORD, "keyword");
    assertThat(keywordTable.tableContainsKeyword(GherkinTokenTypes.FEATURE_KEYWORD, "keyword"), is(true));
  }
}
