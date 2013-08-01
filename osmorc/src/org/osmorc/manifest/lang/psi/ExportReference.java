package org.osmorc.manifest.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.BasicAttributeValueReference;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Vladislav.Soroka on 7/31/13.
 */
public class ExportReference extends BasicAttributeValueReference {
  public final static String NO_IMPORT = "-noimport";
  public final static String SPLIT_PACKAGE = "-split-package";
  public final static String USES = "uses";
  public final static String VERSION = "version";
  @NonNls
  private static final HashSet<String> directiveNames = new HashSet<String>(
    Arrays.asList(NO_IMPORT, SPLIT_PACKAGE, USES, VERSION)
  );

  public ExportReference(final PsiElement element, int offset) {
    super(element, offset);
  }

  @Nullable
  public PsiElement resolve() {
    return myElement; // important for color doc
  }

  @NotNull
  public Object[] getVariants() {
    return ArrayUtil.toObjectArray(directiveNames);
  }

  public boolean isSoft() {
    return true;
  }
}
