package org.intellij.plugins.postcss.usages;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.psi.impl.cache.impl.id.ScanningIdIndexer;

public class PostCssIdIndexer extends ScanningIdIndexer {
  @Override
  protected WordsScanner createScanner() {
    return new PostCssFindUsagesProvider().getWordsScanner();
  }

  @Override
  public int getVersion() {
    return 0;
  }
}
