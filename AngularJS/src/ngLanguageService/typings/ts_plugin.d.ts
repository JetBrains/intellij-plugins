/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
/** A plugin to TypeScript's langauge service that provide language services for
 * templates in string literals.
 *
 * @experimental
 */
declare class LanguageServicePlugin {
    private ts;
    private serviceHost;
    private service;
    private host;
    static 'extension-kind': string;

    constructor(config: {
        ts: any;
        host: any;
        service: any;
        registry?: any;
        args?: any;
    });

    /**
     * Augment the diagnostics reported by TypeScript with errors from the templates in string
     * literals.
     */
    getSemanticDiagnosticsFilter(fileName: string, previous: any[]): any[];

    /**
     * Get completions for angular templates if one is at the given position.
     */
    getCompletionsAtPosition(fileName: string, position: number): any;
}
