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

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.protobuf.lang.names.NameGeneratorContributor;
import com.intellij.protobuf.lang.names.NameGeneratorUtils;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbOptionExpression;

/**
 * Given a proto file, determines which {@link JavaNameGenerator}s are most appropriate. This is based
 * on checking the file-level options.
 */
public final class NameGeneratorSelector {
  private static final Logger log = Logger.getInstance(NameGeneratorSelector.class);

  private NameGeneratorSelector() {}

  /** Base options for protobufs. */
  private static class Options {
    // See the descriptor.proto for option defaults.
    public String javaPackage;
    public String javaOuterClassname;
    public boolean javaMultipleFiles = false;
  }

  private static Options parseOptions(PbFile file) {
    Options options = new Options();
    options.javaPackage = file.getPackageQualifiedName().toString();
    options.javaOuterClassname = Proto2DefinitionClassNames.getDefaultOuterClassName(file);
    for (PbOptionExpression optionExpression : file.getOptions()) {
      // TODO(jvoung): This isn't strictly correct way to match options by name.
      // E.g. another way to set java_package is:
      //   import "net/proto2/proto/descriptor.proto"
      //   option (proto2.FileOptions.java_package) = "com.foo.bar";
      String optionName = optionExpression.getOptionName().getText();
      switch (optionName) {
        case "java_package":
          NameGeneratorUtils.parseStringOption(optionExpression)
            .ifPresent(s -> options.javaPackage = s);
          break;
        case "java_outer_classname":
          NameGeneratorUtils.parseStringOption(optionExpression)
            .ifPresent(s -> options.javaOuterClassname = s);
          break;
        case "java_multiple_files":
          NameGeneratorUtils.parseBoolOption(optionExpression)
            .ifPresent(b -> options.javaMultipleFiles = b);
          break;
        default:
      }
    }
    return options;
  }

  private static ImmutableList<JavaNameGenerator> contributeDefaultGenerators(PbFile file) {
    Options options = parseOptions(file);
    return ImmutableList.of(
      new Proto2NameGenerator(
        file, options.javaPackage, options.javaOuterClassname, options.javaMultipleFiles));
  }

  /** Return the list of generators that are most appropriate to the given file. */
  public static ImmutableList<JavaNameGenerator> selectForFile(PbFile file) {
    for (NameGeneratorContributor contributor : NameGeneratorContributor.EP_NAME.getExtensionList()) {
      if (contributor.isApplicable(file)) {
        log.debug(
          "NameSelector using "
          + contributor.getClass().getName()
          + " for protobuf name generators");
        return contributor.contributeGenerators(file, JavaNameGenerator.class);
      }
    }
    return contributeDefaultGenerators(file);
  }
}
