package com.intellij.javascript.flex;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.BasicAttributeValueReference;
import com.intellij.xml.util.ColorSampleLookupValue;
import com.intellij.xml.util.UserColorLookup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexColorReference extends BasicAttributeValueReference {
  private static Object[] ourValues;

  public FlexColorReference(final PsiElement element, TextRange range) {
    super(element, range);
  }

  @Nullable
   public PsiElement resolve() {
     return myElement;
   }

   @SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod"})
   @NotNull
   public Object[] getVariants() {
     if (ourValues == null) {
       ColorSampleLookupValue[] colors = ColorSampleLookupValue.getColors();
       List<Object> mxmlColors = new ArrayList<Object>();

       for (ColorSampleLookupValue color : colors) {
         String value = color.getValue();
         if (value.startsWith("#") && value.length() > 1) {
           value = "0x" + value.substring(1);
         }
         mxmlColors.add(new ColorSampleLookupValue(color.getName(), value, color.isIsStandard()));
       }

       mxmlColors.add(new UserColorLookup());
       ourValues = mxmlColors.toArray();
     }

     return ourValues;
   }

   public boolean isSoft() {
     return true;
   }
}
