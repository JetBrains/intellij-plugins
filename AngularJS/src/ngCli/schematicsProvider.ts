// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Collection, Schematic} from '@angular-devkit/schematics';

export interface SchematicsProvider {
  getCollection(collectionName: string): Collection<any, any>;

  listSchematics(collection: Collection<any, any>): string[]

  getSchematic(collection: Collection<any, any>, schematicName: string, allowPrivate?: boolean): Schematic<any, any>;

  getDefaultSchematicCollection(): string;
}
