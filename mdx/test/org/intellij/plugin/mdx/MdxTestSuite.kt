package org.intellij.plugin.mdx

import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses(
  MdxAutoImportTest::class,
  MdxCompletionTest::class,
  MdxEmmetTest::class,
  MdxFormatterTest::class,
  MdxHighlightTest::class,
  MdxIntegrationTest::class,
  MdxLiveEditingTest::class,
  MdxMarkdownActionsTest::class,
  MdxMarkdownFloatingToolbarTest::class,
  MdxMarkdownListEditingTest::class,
  MdxMarkdownEditorFeaturesTest::class,
  MdxMarkdownReferencesTest::class,
  MdxMarkdownTableTest::class,
  MdxOracleTest::class,
  MdxParsingTest::class,
  MdxRenameTest::class,
)
class MdxTestSuite
