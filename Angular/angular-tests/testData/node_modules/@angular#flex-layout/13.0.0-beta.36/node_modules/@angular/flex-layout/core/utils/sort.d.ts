/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
interface WithPriority {
    priority?: number;
}
/** HOF to sort the breakpoints by descending priority */
export declare function sortDescendingPriority<T extends WithPriority>(a: T | null, b: T | null): number;
/** HOF to sort the breakpoints by ascending priority */
export declare function sortAscendingPriority<T extends WithPriority>(a: T, b: T): number;
export {};
