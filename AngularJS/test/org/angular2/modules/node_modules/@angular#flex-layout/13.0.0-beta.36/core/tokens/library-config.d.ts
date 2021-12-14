/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { InjectionToken } from '@angular/core';
/** a set of configuration options for FlexLayoutModule */
export interface LayoutConfigOptions {
    addFlexToParent?: boolean;
    addOrientationBps?: boolean;
    disableDefaultBps?: boolean;
    disableVendorPrefixes?: boolean;
    serverLoaded?: boolean;
    useColumnBasisZero?: boolean;
    printWithBreakpoints?: string[];
    mediaTriggerAutoRestore?: boolean;
    ssrObserveBreakpoints?: string[];
}
export declare const DEFAULT_CONFIG: LayoutConfigOptions;
export declare const LAYOUT_CONFIG: InjectionToken<LayoutConfigOptions>;
