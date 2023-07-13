/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { InjectionToken } from '@angular/core';
/**
 * Find all of the server-generated stylings, if any, and remove them
 * This will be in the form of inline classes and the style block in the
 * head of the DOM
 */
export declare function removeStyles(_document: Document, platformId: Object): () => void;
/**
 *  Provider to remove SSR styles on the browser
 */
export declare const BROWSER_PROVIDER: {
    provide: InjectionToken<(() => void)[]>;
    useFactory: typeof removeStyles;
    deps: InjectionToken<Object>[];
    multi: boolean;
};
export declare const CLASS_NAME = "flex-layout-";
