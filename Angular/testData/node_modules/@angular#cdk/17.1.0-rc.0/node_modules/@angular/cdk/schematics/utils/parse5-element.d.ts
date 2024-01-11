/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { DefaultTreeAdapterMap } from 'parse5';
export type Element = DefaultTreeAdapterMap['element'];
export type ChildNode = DefaultTreeAdapterMap['childNode'];
/** Determines the indentation of child elements for the given Parse5 element. */
export declare function getChildElementIndentation(element: Element): number;
