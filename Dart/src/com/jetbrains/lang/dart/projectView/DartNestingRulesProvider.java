// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.projectView;

import com.intellij.ide.projectView.ProjectViewNestingRulesProvider;
import org.jetbrains.annotations.NotNull;

public final class DartNestingRulesProvider implements ProjectViewNestingRulesProvider {
  @Override
  public void addFileNestingRules(@NotNull Consumer consumer) {
    consumer.addNestingRule(".dart", ".dart.js");
    consumer.addNestingRule(".dart", ".dart.js.map");
    consumer.addNestingRule(".dart", ".dart.js.deps");
    consumer.addNestingRule(".dart", ".dart.js.tar.gz");
    consumer.addNestingRule(".dart", ".module");
  }
}
