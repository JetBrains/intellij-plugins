
package org.jetbrains.plugins.cucumber;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.jetbrains.plugins.cucumber.CucumberUtil.isAlphabetCharOrSpace;
import static org.junit.Assert.assertThat;

public class CucumberUtilTest {

  @Test
  public void supportCharactersFromTheLatinAlphabet() throws Exception {
    assertThat(isAlphabetCharOrSpace('a'), is(true));
    assertThat(isAlphabetCharOrSpace('K'), is(true));
  }

  @Test
  public void supportCharactersFromTheRussianAlphabet() throws Exception {
    assertThat(isAlphabetCharOrSpace('ц'), is(true));
  }

  @Test
  public void supportSpace() throws Exception {
    assertThat(isAlphabetCharOrSpace(' '), is(true));
  }

  @Test
  public void supportGermanUmlauts() throws Exception {
    assertThat(isAlphabetCharOrSpace('ä'), is(true));
  }
}
