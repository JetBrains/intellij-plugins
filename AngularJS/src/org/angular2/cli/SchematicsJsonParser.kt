// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

object SchematicsJsonParser {

  fun parse(output: String): List<Schematic> {
    val listType = object : TypeToken<ArrayList<Schematic>>() {}.type
    return GsonBuilder().create().fromJson(output, listType)
  }
}
