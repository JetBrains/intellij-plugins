package com.intellij.javascript.flex;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.BasicAttributeValueReference;
import com.intellij.util.containers.JBIterable;
import com.intellij.xml.util.ColorSampleLookupValue;
import com.intellij.xml.util.UserColorLookup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexColorReference extends BasicAttributeValueReference {

  public FlexColorReference(final PsiElement element, TextRange range) {
    super(element, range);
  }

  @Override
  @Nullable
   public PsiElement resolve() {
     return myElement;
   }

   @Override
   public LookupElement @NotNull [] getVariants() {
     return JBIterable
       .of(ColorSampleLookupValue.getColors())
       .map(color -> {
         String value = color.getValue();
         if (value.startsWith("#") && value.length() > 1) {
           value = "0x" + value.substring(1);
         }
         return new ColorSampleLookupValue(color.getName(), value, color.isIsStandard()).toLookupElement();
       })
       .append(new UserColorLookup())
       .toArray(LookupElement.EMPTY_ARRAY);
   }

   @Override
   public boolean isSoft() {
     return true;
   }
}
