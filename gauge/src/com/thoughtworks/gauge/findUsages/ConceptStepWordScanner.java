/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.findUsages;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.psi.tree.TokenSet;
import com.thoughtworks.gauge.language.token.ConceptTokenTypes;
import com.thoughtworks.gauge.lexer.ConceptLexer;

public final class ConceptStepWordScanner extends DefaultWordsScanner {
  public ConceptStepWordScanner() {
    super(new ConceptLexer(),
          TokenSet.create(ConceptTokenTypes.STEP),
          TokenSet.create(ConceptTokenTypes.COMMENT),
          TokenSet.create(ConceptTokenTypes.STEP));
  }
}
