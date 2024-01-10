import { ElementRef } from '@angular/core';
import { fadeInItems as fadeInLegacyItems } from '@angular/material/menu';
import * as i0 from '@angular/core';
import * as i5 from '@angular/common';
import * as i6 from '@angular/material/core';
import * as i7 from '@angular/cdk/overlay';
import * as i8 from '@angular/cdk/scrolling';
import { MenuPositionX as LegacyMenuPositionX } from '@angular/material/menu';
import { MenuPositionY as LegacyMenuPositionY } from '@angular/material/menu';
import { MAT_MENU_CONTENT as MAT_LEGACY_MENU_CONTENT } from '@angular/material/menu';
import { MAT_MENU_DEFAULT_OPTIONS as MAT_LEGACY_MENU_DEFAULT_OPTIONS } from '@angular/material/menu';
import { MAT_MENU_PANEL as MAT_LEGACY_MENU_PANEL } from '@angular/material/menu';
import { MAT_MENU_SCROLL_STRATEGY as MAT_LEGACY_MENU_SCROLL_STRATEGY } from '@angular/material/menu';
import { matMenuAnimations as matLegacyMenuAnimations } from '@angular/material/menu';
import { MatMenuDefaultOptions as MatLegacyMenuDefaultOptions } from '@angular/material/menu';
import { MatMenuPanel as MatLegacyMenuPanel } from '@angular/material/menu';
import { _MatMenuBase } from '@angular/material/menu';
import { _MatMenuContentBase } from '@angular/material/menu';
import { MatMenuItem } from '@angular/material/menu';
import { _MatMenuTriggerBase } from '@angular/material/menu';
import { NgZone } from '@angular/core';
import { transformMenu as transformLegacyMenu } from '@angular/material/menu';

export { fadeInLegacyItems }

declare namespace i1 {
    export {
        MatLegacyMenu
    }
}

declare namespace i2 {
    export {
        MatLegacyMenuItem
    }
}

declare namespace i3 {
    export {
        MatLegacyMenuTrigger
    }
}

declare namespace i4 {
    export {
        MatLegacyMenuContent
    }
}

export { LegacyMenuPositionX }

export { LegacyMenuPositionY }

export { MAT_LEGACY_MENU_CONTENT }

export { MAT_LEGACY_MENU_DEFAULT_OPTIONS }

export { MAT_LEGACY_MENU_PANEL }

export { MAT_LEGACY_MENU_SCROLL_STRATEGY }

/**
 * @docs-public MatMenu
 * @deprecated Use `MatMenu` from `@angular/material/menu` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyMenu extends _MatMenuBase {
    protected _elevationPrefix: string;
    protected _baseElevation: number;
    /**
     * @deprecated `changeDetectorRef` parameter will become a required parameter.
     * @breaking-change 15.0.0
     */
    constructor(elementRef: ElementRef<HTMLElement>, ngZone: NgZone, defaultOptions: MatLegacyMenuDefaultOptions);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyMenu, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyMenu, "mat-menu", ["matMenu"], {}, {}, never, ["*"], false, never>;
}

export { matLegacyMenuAnimations }

/**
 * Menu content that will be rendered lazily once the menu is opened.
 * @deprecated Use `MatMenuContent` from `@angular/material/menu` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyMenuContent extends _MatMenuContentBase {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyMenuContent, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyMenuContent, "ng-template[matMenuContent]", never, {}, {}, never, never, false, never>;
}

export { MatLegacyMenuDefaultOptions }

/**
 * Single item inside of a `mat-menu`. Provides the menu item styling and accessibility treatment.
 * @deprecated Use `MatMenuItem` from `@angular/material/menu` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyMenuItem extends MatMenuItem {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyMenuItem, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyMenuItem, "[mat-menu-item]", ["matMenuItem"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/**
 * @deprecated Use `MatMenuModule` from `@angular/material/menu` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyMenuModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyMenuModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyMenuModule, [typeof i1.MatLegacyMenu, typeof i2.MatLegacyMenuItem, typeof i3.MatLegacyMenuTrigger, typeof i4.MatLegacyMenuContent], [typeof i5.CommonModule, typeof i6.MatCommonModule, typeof i6.MatRippleModule, typeof i7.OverlayModule], [typeof i8.CdkScrollableModule, typeof i6.MatCommonModule, typeof i1.MatLegacyMenu, typeof i2.MatLegacyMenuItem, typeof i3.MatLegacyMenuTrigger, typeof i4.MatLegacyMenuContent]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyMenuModule>;
}

export { MatLegacyMenuPanel }

/**
 * Directive applied to an element that should trigger a `mat-menu`.
 * @deprecated Use `MatMenuTrigger` from `@angular/material/menu` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyMenuTrigger extends _MatMenuTriggerBase {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyMenuTrigger, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyMenuTrigger, "[mat-menu-trigger-for], [matMenuTriggerFor]", ["matMenuTrigger"], {}, {}, never, never, false, never>;
}

export { transformLegacyMenu }

export { }
