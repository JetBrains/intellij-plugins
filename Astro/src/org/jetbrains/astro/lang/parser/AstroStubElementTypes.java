// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser;

public interface AstroStubElementTypes {
  int STUB_VERSION = 1;

  String EXTERNAL_ID_PREFIX = "ASTRO:";

  AstroContentRootType CONTENT_ROOT = new AstroContentRootType();

  AstroFrontmatterScriptType FRONTMATTER_SCRIPT = new AstroFrontmatterScriptType();

  AstroHtmlAttributeElementType HTML_ATTRIBUTE = new AstroHtmlAttributeElementType();
}
