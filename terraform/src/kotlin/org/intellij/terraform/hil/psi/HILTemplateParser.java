// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi;

import com.intellij.lang.PsiParser;
import org.intellij.terraform.template.psi.TerraformTemplateHilBasedParser;

/**
 * This is a marker interface for HIL-based parsers aimed for use with templating-languages.
 * Implementation of this interface shows the parser's ability to understand
 * both template and data-language blocks in a provided file.
 * On the contrary, a parser that doesn't implement this interface
 * is set to parse the complete file with the assumption of having only
 * template language tokens present in it.
 *
 * @see org.intellij.terraform.hil.psi.HilTemplatingAwarePsiBuilder
 * @see TerraformTemplateHilBasedParser
 */
public interface HILTemplateParser extends PsiParser {
}
