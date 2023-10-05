import { AnimationTriggerMetadata } from '@angular/animations';
import { AriaDescriber } from '@angular/cdk/a11y';
import { BreakpointObserver } from '@angular/cdk/layout';
import { BreakpointState } from '@angular/cdk/layout';
import { ChangeDetectorRef } from '@angular/core';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { getMatTooltipInvalidPositionError as getMatLegacyTooltipInvalidPositionError } from '@angular/material/tooltip';
import * as i0 from '@angular/core';
import * as i2 from '@angular/cdk/a11y';
import * as i3 from '@angular/common';
import * as i4 from '@angular/cdk/overlay';
import * as i5 from '@angular/material/core';
import * as i6 from '@angular/cdk/scrolling';
import { SCROLL_THROTTLE_MS as LEGACY_SCROLL_THROTTLE_MS } from '@angular/material/tooltip';
import { TooltipPosition as LegacyTooltipPosition } from '@angular/material/tooltip';
import { TooltipTouchGestures as LegacyTooltipTouchGestures } from '@angular/material/tooltip';
import { TooltipVisibility as LegacyTooltipVisibility } from '@angular/material/tooltip';
import { MAT_TOOLTIP_DEFAULT_OPTIONS as MAT_LEGACY_TOOLTIP_DEFAULT_OPTIONS } from '@angular/material/tooltip';
import { MAT_TOOLTIP_DEFAULT_OPTIONS_FACTORY as MAT_LEGACY_TOOLTIP_DEFAULT_OPTIONS_FACTORY } from '@angular/material/tooltip';
import { MAT_TOOLTIP_SCROLL_STRATEGY as MAT_LEGACY_TOOLTIP_SCROLL_STRATEGY } from '@angular/material/tooltip';
import { MAT_TOOLTIP_SCROLL_STRATEGY_FACTORY as MAT_LEGACY_TOOLTIP_SCROLL_STRATEGY_FACTORY } from '@angular/material/tooltip';
import { MAT_TOOLTIP_SCROLL_STRATEGY_FACTORY_PROVIDER as MAT_LEGACY_TOOLTIP_SCROLL_STRATEGY_FACTORY_PROVIDER } from '@angular/material/tooltip';
import { MatTooltipDefaultOptions as MatLegacyTooltipDefaultOptions } from '@angular/material/tooltip';
import { _MatTooltipBase } from '@angular/material/tooltip';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { Overlay } from '@angular/cdk/overlay';
import { Platform } from '@angular/cdk/platform';
import { ScrollDispatcher } from '@angular/cdk/scrolling';
import { _TooltipComponentBase } from '@angular/material/tooltip';
import { ViewContainerRef } from '@angular/core';

export { getMatLegacyTooltipInvalidPositionError }

declare namespace i1 {
    export {
        MatLegacyTooltip,
        LegacyTooltipComponent
    }
}

export { LEGACY_SCROLL_THROTTLE_MS }

/**
 * Internal component that wraps the tooltip's content.
 * @docs-private
 * @deprecated Use `TooltipComponent` from `@angular/material/tooltip` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class LegacyTooltipComponent extends _TooltipComponentBase {
    /** Stream that emits whether the user has a handset-sized display.  */
    _isHandset: Observable<BreakpointState>;
    _showAnimation: string;
    _hideAnimation: string;
    _tooltip: ElementRef<HTMLElement>;
    constructor(changeDetectorRef: ChangeDetectorRef, breakpointObserver: BreakpointObserver, animationMode?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<LegacyTooltipComponent, [null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<LegacyTooltipComponent, "mat-tooltip-component", never, {}, {}, never, never, false, never>;
}

export { LegacyTooltipPosition }

export { LegacyTooltipTouchGestures }

export { LegacyTooltipVisibility }

export { MAT_LEGACY_TOOLTIP_DEFAULT_OPTIONS }

export { MAT_LEGACY_TOOLTIP_DEFAULT_OPTIONS_FACTORY }

export { MAT_LEGACY_TOOLTIP_SCROLL_STRATEGY }

export { MAT_LEGACY_TOOLTIP_SCROLL_STRATEGY_FACTORY }

export { MAT_LEGACY_TOOLTIP_SCROLL_STRATEGY_FACTORY_PROVIDER }

/**
 * Directive that attaches a material design tooltip to the host element. Animates the showing and
 * hiding of a tooltip provided position (defaults to below the element).
 *
 * https://material.io/design/components/tooltips.html
 *
 * @deprecated Use `MatTooltip` from `@angular/material/tooltip` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTooltip extends _MatTooltipBase<LegacyTooltipComponent> {
    protected readonly _tooltipComponent: typeof LegacyTooltipComponent;
    constructor(overlay: Overlay, elementRef: ElementRef<HTMLElement>, scrollDispatcher: ScrollDispatcher, viewContainerRef: ViewContainerRef, ngZone: NgZone, platform: Platform, ariaDescriber: AriaDescriber, focusMonitor: FocusMonitor, scrollStrategy: any, dir: Directionality, defaultOptions: MatLegacyTooltipDefaultOptions, _document: any);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTooltip, [null, null, null, null, null, null, null, null, null, { optional: true; }, { optional: true; }, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyTooltip, "[matTooltip]", ["matTooltip"], {}, {}, never, never, false, never>;
}

/**
 * Animations used by MatTooltip.
 * @docs-private
 * @deprecated Use `matTooltipAnimations` from `@angular/material/tooltip` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const matLegacyTooltipAnimations: {
    readonly tooltipState: AnimationTriggerMetadata;
};

export { MatLegacyTooltipDefaultOptions }

/**
 * @deprecated Use `MatTooltipModule` from `@angular/material/tooltip` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTooltipModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTooltipModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyTooltipModule, [typeof i1.MatLegacyTooltip, typeof i1.LegacyTooltipComponent], [typeof i2.A11yModule, typeof i3.CommonModule, typeof i4.OverlayModule, typeof i5.MatCommonModule], [typeof i1.MatLegacyTooltip, typeof i1.LegacyTooltipComponent, typeof i5.MatCommonModule, typeof i6.CdkScrollableModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyTooltipModule>;
}

export { }
