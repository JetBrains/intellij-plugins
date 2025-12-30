import { BreakpointObserver } from '@angular/cdk/layout';
import * as i0 from '@angular/core';
import * as i3 from '@angular/cdk/overlay';
import * as i4 from '@angular/cdk/portal';
import * as i5 from '@angular/common';
import * as i6 from '@angular/material/legacy-button';
import * as i7 from '@angular/material/core';
import { Injector } from '@angular/core';
import { TextOnlySnackBar as LegacyTextOnlySnackBar } from '@angular/material/snack-bar';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { MAT_SNACK_BAR_DATA as MAT_LEGACY_SNACK_BAR_DATA } from '@angular/material/snack-bar';
import { MAT_SNACK_BAR_DEFAULT_OPTIONS as MAT_LEGACY_SNACK_BAR_DEFAULT_OPTIONS } from '@angular/material/snack-bar';
import { MAT_SNACK_BAR_DEFAULT_OPTIONS_FACTORY as MAT_LEGACY_SNACK_BAR_DEFAULT_OPTIONS_FACTORY } from '@angular/material/snack-bar';
import { matSnackBarAnimations as matLegacySnackBarAnimations } from '@angular/material/snack-bar';
import { _MatSnackBarBase as _MatLegacySnackBarBase } from '@angular/material/snack-bar';
import { MatSnackBarConfig as MatLegacySnackBarConfig } from '@angular/material/snack-bar';
import { _MatSnackBarContainerBase as _MatLegacySnackBarContainerBase } from '@angular/material/snack-bar';
import { MatSnackBarDismiss as MatLegacySnackBarDismiss } from '@angular/material/snack-bar';
import { MatSnackBarHorizontalPosition as MatLegacySnackBarHorizontalPosition } from '@angular/material/snack-bar';
import { MatSnackBarRef as MatLegacySnackBarRef } from '@angular/material/snack-bar';
import { MatSnackBarVerticalPosition as MatLegacySnackBarVerticalPosition } from '@angular/material/snack-bar';
import { Overlay } from '@angular/cdk/overlay';

declare namespace i1 {
    export {
        MatLegacySnackBarContainer
    }
}

declare namespace i2 {
    export {
        LegacySimpleSnackBar
    }
}

/**
 * A component used to open as the default snack bar, matching material spec.
 * This should only be used internally by the snack bar service.
 * @deprecated Use `SimpleSnackBar` from `@angular/material/snack-bar` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class LegacySimpleSnackBar implements LegacyTextOnlySnackBar {
    snackBarRef: MatLegacySnackBarRef<LegacySimpleSnackBar>;
    /** Data that was injected into the snack bar. */
    data: {
        message: string;
        action: string;
    };
    constructor(snackBarRef: MatLegacySnackBarRef<LegacySimpleSnackBar>, data: any);
    /** Performs the action on the snack bar. */
    action(): void;
    /** If the action button should be shown. */
    get hasAction(): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<LegacySimpleSnackBar, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<LegacySimpleSnackBar, "simple-snack-bar", never, {}, {}, never, never, false, never>;
}

export { LegacyTextOnlySnackBar }

export { MAT_LEGACY_SNACK_BAR_DATA }

export { MAT_LEGACY_SNACK_BAR_DEFAULT_OPTIONS }

export { MAT_LEGACY_SNACK_BAR_DEFAULT_OPTIONS_FACTORY }

/**
 * Service to dispatch Material Design snack bar messages.
 * @deprecated Use `MatSnackBar` from `@angular/material/snack-bar` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySnackBar extends _MatLegacySnackBarBase {
    protected simpleSnackBarComponent: typeof LegacySimpleSnackBar;
    protected snackBarContainerComponent: typeof MatLegacySnackBarContainer;
    protected handsetCssClass: string;
    constructor(overlay: Overlay, live: LiveAnnouncer, injector: Injector, breakpointObserver: BreakpointObserver, parentSnackBar: MatLegacySnackBar, defaultConfig: MatLegacySnackBarConfig);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySnackBar, [null, null, null, null, { optional: true; skipSelf: true; }, null]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatLegacySnackBar>;
}

export { matLegacySnackBarAnimations }

export { _MatLegacySnackBarBase }

export { MatLegacySnackBarConfig }

/**
 * Internal component that wraps user-provided snack bar content.
 * @docs-private
 * @deprecated Use `MatSnackBarContainer` from `@angular/material/snack-bar` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySnackBarContainer extends _MatLegacySnackBarContainerBase {
    protected _afterPortalAttached(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySnackBarContainer, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacySnackBarContainer, "snack-bar-container", never, {}, {}, never, never, false, never>;
}

export { _MatLegacySnackBarContainerBase }

export { MatLegacySnackBarDismiss }

export { MatLegacySnackBarHorizontalPosition }

/**
 * @deprecated Use `MatSnackBarModule` from `@angular/material/snack-bar` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySnackBarModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySnackBarModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacySnackBarModule, [typeof i1.MatLegacySnackBarContainer, typeof i2.LegacySimpleSnackBar], [typeof i3.OverlayModule, typeof i4.PortalModule, typeof i5.CommonModule, typeof i6.MatLegacyButtonModule, typeof i7.MatCommonModule], [typeof i1.MatLegacySnackBarContainer, typeof i7.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacySnackBarModule>;
}

export { MatLegacySnackBarRef }

export { MatLegacySnackBarVerticalPosition }

export { }
