/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
export declare type NgStyleRawList = string[];
export declare type NgStyleMap = {
    [klass: string]: string;
};
export declare type NgStyleType = string | Set<string> | NgStyleRawList | NgStyleMap;
/**
 * Callback function for SecurityContext.STYLE sanitization
 */
export declare type NgStyleSanitizer = (val: any) => string;
/** NgStyle allowed inputs */
export declare class NgStyleKeyValue {
    key: string;
    value: string;
    constructor(key: string, value: string, noQuotes?: boolean);
}
export declare function getType(target: any): string;
/**
 * Split string of key:value pairs into Array of k-v pairs
 * e.g.  'key:value; key:value; key:value;' -> ['key:value',...]
 */
export declare function buildRawList(source: any, delimiter?: string): NgStyleRawList;
/** Convert array of key:value strings to a iterable map object */
export declare function buildMapFromList(styles: NgStyleRawList, sanitize?: NgStyleSanitizer): NgStyleMap;
/** Convert Set<string> or raw Object to an iterable NgStyleMap */
export declare function buildMapFromSet(source: NgStyleType, sanitize?: NgStyleSanitizer): NgStyleMap;
/** Convert 'key:value' -> [key, value] */
export declare function stringToKeyValue(it: string): NgStyleKeyValue;
/** Convert [ [key,value] ] -> { key : value } */
export declare function keyValuesToMap(map: NgStyleMap, entry: NgStyleKeyValue): NgStyleMap;
