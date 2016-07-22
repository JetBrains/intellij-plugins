package com.intellij.struts2.model.constant;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extend resolving to class.
 *
 * @author Yann C&eacute;bron
 */
public interface ConstantValueConverterClassContributor {

  /**
   * Extend possible resolving to class.
   */
  ExtensionPointName<ConstantValueConverterClassContributor> EP_NAME =
    new ExtensionPointName<>(
      "com.intellij.struts2.constantValueClassContributor");

  /**
   * Performs the actual conversion.
   *
   * @param s              Constant value to convert.
   * @param convertContext Current context.
   * @return {@code null} if unable to convert class.
   */
  @Nullable
  PsiClass fromString(@NotNull @NonNls final String s, final ConvertContext convertContext);

}