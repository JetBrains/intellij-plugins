// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type {Language} from "@volar/language-core"

// TODO: patch `typescript` module instead
declare module "tsc-ide-plugin/tsserverlibrary.shim" {
  namespace server {
    interface Project {
      readonly __vue__: Readonly<{
        language: Language<string>
      }>
    }
  }
}
