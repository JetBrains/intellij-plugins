// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Angular2AttributeNameVariantsBuilder {

  @NotNull
  public static List<String> forTypes(@NotNull String name,
                                      boolean includeCanonicalVariants,
                                      boolean includeDataPrefixVariants,
                                      Angular2AttributeType... types) {
    List<String> result = new ArrayList<>();
    for (Angular2AttributeType type : types) {
      result.add(type.buildName(name, false));
      if (includeCanonicalVariants) {
        ContainerUtil.addIfNotNull(result, type.buildName(name, true));
      }
    }
    if (includeDataPrefixVariants) {
      return ContainerUtil.concat(result, ContainerUtil.map2List(result, r -> "data-" + r));
    }
    return result;
  }
}
