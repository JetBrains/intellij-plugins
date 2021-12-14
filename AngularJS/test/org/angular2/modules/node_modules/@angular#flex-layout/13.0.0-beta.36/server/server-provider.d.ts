/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { InjectionToken } from '@angular/core';
import { BreakPoint, ɵMatchMedia as MatchMedia, StylesheetMap } from '@angular/flex-layout/core';
import { ServerMatchMedia } from './server-match-media';
/**
 * Activate all of the registered breakpoints in sequence, and then
 * retrieve the associated stylings from the virtual stylesheet
 * @param serverSheet the virtual stylesheet that stores styles for each
 *        element
 * @param mediaController the MatchMedia service to activate/deactivate breakpoints
 * @param breakpoints the registered breakpoints to activate/deactivate
 * @param layoutConfig the library config, and specifically the breakpoints to activate
 */
export declare function generateStaticFlexLayoutStyles(serverSheet: StylesheetMap, mediaController: ServerMatchMedia, breakpoints: BreakPoint[]): string;
/**
 * Create a style tag populated with the dynamic stylings from Flex
 * components and attach it to the head of the DOM
 */
export declare function FLEX_SSR_SERIALIZER_FACTORY(serverSheet: StylesheetMap, mediaController: ServerMatchMedia, _document: Document, breakpoints: BreakPoint[]): () => void;
/**
 *  Provider to set static styles on the server
 */
export declare const SERVER_PROVIDERS: ({
    provide: InjectionToken<() => void>;
    useFactory: typeof FLEX_SSR_SERIALIZER_FACTORY;
    deps: (InjectionToken<Document> | typeof StylesheetMap | typeof MatchMedia)[];
    multi: boolean;
    useValue?: undefined;
    useClass?: undefined;
} | {
    provide: InjectionToken<boolean>;
    useValue: boolean;
    useFactory?: undefined;
    deps?: undefined;
    multi?: undefined;
    useClass?: undefined;
} | {
    provide: typeof MatchMedia;
    useClass: typeof ServerMatchMedia;
    useFactory?: undefined;
    deps?: undefined;
    multi?: undefined;
    useValue?: undefined;
})[];
export declare type StyleSheet = Map<HTMLElement, Map<string, string | number>>;
export declare type ClassMap = Map<HTMLElement, string>;
