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
package org.osmorc.manifest.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;
import org.osgi.framework.Constants;
import org.osmorc.manifest.lang.psi.Directive;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author Vladislav.Soroka
 */
public final class OsgiManifestCompletionContributor extends CompletionContributor {
  public OsgiManifestCompletionContributor() {
    extend(
      CompletionType.BASIC,
      header(Constants.EXPORT_PACKAGE),
      new HeaderParametersProvider(Constants.VERSION_ATTRIBUTE, Constants.USES_DIRECTIVE + ':'));

    extend(
      CompletionType.BASIC,
      header(Constants.IMPORT_PACKAGE),
      new HeaderParametersProvider(Constants.VERSION_ATTRIBUTE, Constants.RESOLUTION_DIRECTIVE + ':'));

    extend(
      CompletionType.BASIC,
      directive(Constants.RESOLUTION_DIRECTIVE),
      new SimpleProvider(Constants.RESOLUTION_MANDATORY, Constants.RESOLUTION_OPTIONAL));
  }

  private static ElementPattern<PsiElement> header(String name) {
    return psiElement(ManifestTokenType.HEADER_VALUE_PART)
      .afterLeaf(";")
      .withSuperParent(3, psiElement(Header.class).withName(name));
  }

  private static ElementPattern<PsiElement> directive(String name) {
    return psiElement(ManifestTokenType.HEADER_VALUE_PART)
      .withSuperParent(2, psiElement(Directive.class).withName(name));
  }
}
