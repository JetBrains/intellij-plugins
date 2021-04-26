/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.jvm.names;

import com.intellij.openapi.util.Ref;
import com.intellij.protobuf.lang.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a proto file, determines which {@link NameGenerator}s are most appropriate. This is based
 * on checking the file-level options.
 */
public class NameGeneratorSelector {

  /** Return the list of generators that are most appropriate to the given file. */
  public static List<NameGenerator> selectForFile(PbFile file) {
    // See the descriptor.proto for option defaults.
    Ref<Integer> javaApiVersion = Ref.create(2);
    Ref<String> javaPackage = Ref.create();
    Ref<String> javaOuterClassname = Ref.create();
    Ref<Boolean> javaMultipleFiles = Ref.create(false);
    Ref<Boolean> javaMutableApi = Ref.create(false);
    Ref<String> javaMultipleFilesMutablePackage = Ref.create();
    List<NameGenerator> generators = new ArrayList<>();
    for (PbOptionExpression optionExpression : file.getOptions()) {
      // TODO(jvoung): This isn't strictly correct way to match options by name.
      // E.g. another way to set java_package is:
      //   import "net/proto2/proto/descriptor.proto"
      //   option (proto2.FileOptions.java_package) = "com.foo.bar";
      String optionName = optionExpression.getOptionName().getText();
      switch (optionName) {
        case "java_package":
          parseStringOption(optionExpression, javaPackage);
          break;
        case "java_outer_classname":
          parseStringOption(optionExpression, javaOuterClassname);
          break;
        case "java_multiple_files":
          parseBoolOption(optionExpression, javaMultipleFiles);
          break;
        default:
          // Other options are irrelevant.
      }
    }

    // TODO(jvoung): Do we need to generate both api v2 mutable and api v1 sometimes (for bridging)?
    if (javaApiVersion.get() == 1) {
      generators.add(new Proto1NameGenerator(file, javaPackage.get()));
    } else if (javaApiVersion.get() == 2) {
      if (javaOuterClassname.isNull()) {
        javaOuterClassname.set(Proto2DefinitionClassNames.getDefaultOuterClassName(file));
      }
      if (javaMutableApi.get()) {
        if (javaMultipleFilesMutablePackage.isNull()) {
          javaMultipleFilesMutablePackage.set("");
        }
        generators.add(
            new Proto2MutableNameGenerator(
                file,
                javaPackage.get(),
                javaMultipleFilesMutablePackage.get(),
                javaOuterClassname.get(),
                javaMultipleFiles.get()));
      } else {
        generators.add(
            new Proto2NameGenerator(
                file, javaPackage.get(), javaOuterClassname.get(), javaMultipleFiles.get()));
      }
    }

    return generators;
  }

  private static void parseIntOption(PbOptionExpression optionExpression, Ref<Integer> outValue) {
    PbNumberValue numberValue = optionExpression.getNumberValue();
    if (numberValue == null) {
      return;
    }
    if (numberValue.getLongValue() != null) {
      outValue.set(numberValue.getLongValue().intValue());
    }
  }

  private static void parseStringOption(PbOptionExpression optionExpression, Ref<String> outValue) {
    PbStringValue stringValue = optionExpression.getStringValue();
    if (stringValue == null) {
      return;
    }
    outValue.set(stringValue.getAsString());
  }

  private static void parseBoolOption(PbOptionExpression optionExpression, Ref<Boolean> outValue) {
    PbIdentifierValue boolAsIdentifier = optionExpression.getIdentifierValue();
    if (boolAsIdentifier == null) {
      return;
    }
    Boolean value = boolAsIdentifier.getBooleanValue();
    if (value == null) {
      return;
    }
    outValue.set(value);
  }
}
