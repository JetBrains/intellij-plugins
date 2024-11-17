/*
 * Copyright © 2022 Yuriy Artamonov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.intellij.tsr;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import com.intellij.tsr.psi.*;

import java.util.List;

public final class TslUtils {
  private TslUtils() {
  }

  public static boolean isCompactPropertyBlock(PsiElement psiElement) {
    if (psiElement instanceof TslMap) {
      var map = ((TslMap) psiElement);
      if (map.getMapItemList().size() > 1) {
        return false;
      }

      if (map.getMapItemList().isEmpty()) {
        return true;
      }

      TslMapItem mapItem = map.getMapItemList().get(0);
      if (mapItem.getValue() == null) {
        return true;
      }

      return isPrimitive(mapItem.getValue()) && isPrimitive(mapItem.getMapKey().getValue());
    } else if (psiElement instanceof TslObject) {
      var object = ((TslObject) psiElement);
      List<TslPropertyKeyValue> properties = object.getPropertyKeyValueList();

      if (properties.isEmpty()) {
        return true;
      }
      if (properties.size() > 1) {
        return false;
      }

      TslPropertyKeyValue property = properties.get(0);
      return property.getValue() == null || isPrimitive(property.getValue());
    } else if (psiElement instanceof TslList) {
      return ((TslList) psiElement).getValueList().isEmpty();
    }

    return false;
  }

  public static boolean isPrimitive(@NotNull TslValue value) {
    return value instanceof TslNullLiteral
        || value instanceof TslStringLiteral
        || value instanceof TslFallbackStringLiteral
        || value instanceof TslNumberLiteral
        || value instanceof TslBooleanLiteral
        || value instanceof TslObjectRef
        || value instanceof TslObjectId;
  }
}
