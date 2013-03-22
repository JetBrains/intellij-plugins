package org.jetbrains.plugins.cucumber.java.psi;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class GherkinDictionaryKeywordProviderTest {
  private final GherkinDictionary myDictionary = EasyMock.createNiceMock(GherkinDictionary.class);
  private final GherkinDictionaryKeywordProvider myKeywordProvider = new GherkinDictionaryKeywordProvider(myDictionary);

  @Before
  public void setUp() throws Exception {
    EasyMock.replay(myDictionary);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void isStepKeywordIsNeverCalledOnTheInterface() throws Exception {
    myKeywordProvider.isStepKeyword("any");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getBaseKeywordIsNeverCalledOnTheInterface() throws Exception {
    myKeywordProvider.getBaseKeyword("any", "any");
  }
}