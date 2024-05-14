// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import * as ts from "typescript/lib/tsserverlibrary";

module.exports = function init(
  {typescript: ts_impl}: { typescript: typeof ts },
) {

  return {
    create(info: ts.server.PluginCreateInfo): ts.LanguageService {
      return info.languageService;
    }
  };
};
