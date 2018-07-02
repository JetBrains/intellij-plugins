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
package org.angular2.codeInsight;

/**
 * @author Irina.Chernushina on 12/3/2015.
 */
public enum Angular2PluralCategories {
  zero(5),
  one(1),
  two(4),
  few(3),
  many(2),
  other(0);

  private final int myCompletionOrder;

  Angular2PluralCategories(int completionOrder) {
    myCompletionOrder = completionOrder;
  }

  public int getCompletionOrder() {
    return myCompletionOrder;
  }
}
