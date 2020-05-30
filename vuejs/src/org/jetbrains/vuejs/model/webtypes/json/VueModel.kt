// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("prop", "event")
class VueModel {

  @JsonProperty("prop")
  @get:JsonProperty("prop")
  @set:JsonProperty("prop")
  var prop = "value"

  @JsonProperty("event")
  @get:JsonProperty("event")
  @set:JsonProperty("event")
  var event = "input"

}
