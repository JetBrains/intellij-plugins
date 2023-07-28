/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ModuleWithProviders } from '@angular/core';
import { LayoutConfigOptions, BreakPoint } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
import * as i1 from "@angular/flex-layout/flex";
import * as i2 from "@angular/flex-layout/extended";
import * as i3 from "@angular/flex-layout/grid";
/**
 * FlexLayoutModule -- the main import for all utilities in the Angular Layout library
 * * Will automatically provide Flex, Grid, and Extended modules for use in the application
 * * Can be configured using the static withConfig method, options viewable on the Wiki's
 *   Configuration page
 */
export declare class FlexLayoutModule {
    /**
     * Initialize the FlexLayoutModule with a set of config options,
     * which sets the corresponding tokens accordingly
     */
    static withConfig(configOptions: LayoutConfigOptions, breakpoints?: BreakPoint | BreakPoint[]): ModuleWithProviders<FlexLayoutModule>;
    constructor(serverModuleLoaded: boolean, platformId: Object);
    static ɵfac: i0.ɵɵFactoryDeclaration<FlexLayoutModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<FlexLayoutModule, never, [typeof i1.FlexModule, typeof i2.ExtendedModule, typeof i3.GridModule], [typeof i1.FlexModule, typeof i2.ExtendedModule, typeof i3.GridModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<FlexLayoutModule>;
}
