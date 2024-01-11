/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import * as ts from 'typescript';
import { ResolvedResource } from '../../update-tool/component-resource-collector';
import { Migration } from '../../update-tool/migration';
import { ElementSelectorUpgradeData } from '../data/element-selectors';
import { UpgradeData } from '../upgrade-data';
/**
 * Migration that walks through every string literal, template and stylesheet in order
 * to migrate outdated element selectors to the new one.
 */
export declare class ElementSelectorsMigration extends Migration<UpgradeData> {
    /** Change data that upgrades to the specified target version. */
    data: ElementSelectorUpgradeData[];
    enabled: boolean;
    visitNode(node: ts.Node): void;
    visitTemplate(template: ResolvedResource): void;
    visitStylesheet(stylesheet: ResolvedResource): void;
    private _visitStringLiteralLike;
    private _replaceSelector;
}
