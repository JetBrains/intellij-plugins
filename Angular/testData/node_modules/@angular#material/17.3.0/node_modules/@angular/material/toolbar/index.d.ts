import { AfterViewInit } from '@angular/core';
import { ElementRef } from '@angular/core';
import * as i0 from '@angular/core';
import * as i1 from '@angular/material/core';
import { Platform } from '@angular/cdk/platform';
import { QueryList } from '@angular/core';

declare namespace i2 {
    export {
        throwToolbarMixedModesError,
        MatToolbarRow,
        MatToolbar
    }
}

export declare class MatToolbar implements AfterViewInit {
    protected _elementRef: ElementRef;
    private _platform;
    /** Palette color of the toolbar. */
    color?: string | null;
    private _document;
    /** Reference to all toolbar row elements that have been projected. */
    _toolbarRows: QueryList<MatToolbarRow>;
    constructor(_elementRef: ElementRef, _platform: Platform, document?: any);
    ngAfterViewInit(): void;
    /**
     * Throws an exception when developers are attempting to combine the different toolbar row modes.
     */
    private _checkToolbarMixedModes;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatToolbar, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatToolbar, "mat-toolbar", ["matToolbar"], { "color": { "alias": "color"; "required": false; }; }, {}, ["_toolbarRows"], ["*", "mat-toolbar-row"], true, never>;
}

export declare class MatToolbarModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatToolbarModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatToolbarModule, never, [typeof i1.MatCommonModule, typeof i2.MatToolbar, typeof i2.MatToolbarRow], [typeof i2.MatToolbar, typeof i2.MatToolbarRow, typeof i1.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatToolbarModule>;
}

export declare class MatToolbarRow {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatToolbarRow, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatToolbarRow, "mat-toolbar-row", ["matToolbarRow"], {}, {}, never, never, true, never>;
}

/**
 * Throws an exception when attempting to combine the different toolbar row modes.
 * @docs-private
 */
export declare function throwToolbarMixedModesError(): void;

export { }
