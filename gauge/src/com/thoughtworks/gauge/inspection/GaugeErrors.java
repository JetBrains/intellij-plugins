/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.inspection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// todo refactor
final class GaugeErrors {
  private static Map<String, List<GaugeError>> e = new HashMap<>();

  static void add(String key, List<GaugeError> errors) {
    e.put(key, errors);
  }

  static void init() {
    e = new HashMap<>();
  }

  static List<GaugeError> get(String key) {
    return e.get(key) == null ? new ArrayList<>() : e.get(key);
  }
}
