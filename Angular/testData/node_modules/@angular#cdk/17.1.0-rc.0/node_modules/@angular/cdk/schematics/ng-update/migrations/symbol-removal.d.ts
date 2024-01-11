/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import * as ts from 'typescript';
import { Migration } from '../../update-tool/migration';
import { SymbolRemovalUpgradeData } from '../data';
import { UpgradeData } from '../upgrade-data';
/** Migration that flags imports for symbols that have been removed. */
export declare class SymbolRemovalMigration extends Migration<UpgradeData> {
    /** Change data that upgrades to the specified target version. */
    data: SymbolRemovalUpgradeData[];
    enabled: boolean;
    visitNode(node: ts.Node): void;
}
