// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {FileSystemEngineHostBase} from "@angular-devkit/schematics/tools/file-system-engine-host-base"
import {Collection, Schematic} from '@angular-devkit/schematics';

export interface SchematicsProvider {
    getCollection(collectionName: string): Collection<any, any>;
    getEngineHost(): FileSystemEngineHostBase;
    getSchematic(collection: Collection<any, any>, schematicName: string, allowPrivate?: boolean): Schematic<any, any>;
}