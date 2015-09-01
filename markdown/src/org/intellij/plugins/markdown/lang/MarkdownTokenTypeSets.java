/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.markdown.lang;

import com.intellij.psi.tree.TokenSet;

public interface MarkdownTokenTypeSets extends MarkdownElementTypes {
  TokenSet HEADER_MARKERS = TokenSet.create(
          MarkdownTokenTypes.ATX_HEADER,
          MarkdownTokenTypes.SETEXT_1,
          MarkdownTokenTypes.SETEXT_2);
  TokenSet HEADER_LEVEL_1_SET = TokenSet.create(ATX_1, SETEXT_1);
  TokenSet HEADER_LEVEL_2_SET = TokenSet.create(ATX_2, SETEXT_2);
  TokenSet HEADER_LEVEL_3_SET = TokenSet.create(ATX_3);
  TokenSet HEADER_LEVEL_4_SET = TokenSet.create(ATX_4);
  TokenSet HEADER_LEVEL_5_SET = TokenSet.create(ATX_5);
  TokenSet HEADER_LEVEL_6_SET = TokenSet.create(ATX_6);

  TokenSet REFERENCE_LINK_SET = TokenSet.create(FULL_REFERENCE_LINK, SHORT_REFERENCE_LINK);

  TokenSet LIST_MARKERS = TokenSet.create(MarkdownTokenTypes.LIST_BULLET, MarkdownTokenTypes.LIST_NUMBER);

  TokenSet INLINE_HOLDING_ELEMENT_TYPES =
    TokenSet.create(MarkdownElementTypes.PARAGRAPH,
                    MarkdownTokenTypes.ATX_CONTENT,
                    MarkdownTokenTypes.SETEXT_CONTENT);


}
