package com.intellij.struts2.dom.struts.constant;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extend resolving to class for {@link com.intellij.struts2.dom.struts.constant.ConstantValueConverter}.
 *
 * @author Yann C&eacute;bron
 */
public interface ConstantValueConverterClassContributor {

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