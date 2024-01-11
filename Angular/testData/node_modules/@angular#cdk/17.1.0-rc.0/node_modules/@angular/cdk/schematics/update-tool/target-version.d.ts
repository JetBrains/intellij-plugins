/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
/** Possible versions that can be automatically migrated by `ng update`. */
export declare enum TargetVersion {
    V17 = "version 17"
}
/**
 * Returns all versions that are supported by "ng update". The versions are determined
 * based on the "TargetVersion" enum.
 */
export declare function getAllVersionNames(): string[];
