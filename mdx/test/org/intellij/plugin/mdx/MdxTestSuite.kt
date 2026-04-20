package org.intellij.plugin.mdx

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  MdxTest::class,
  MdxEmmetTest::class,
  MdxHighlightTest::class,
  MdxParsingTest::class,
  MdxFormatterTest::class,
)
class MdxTestSuite
