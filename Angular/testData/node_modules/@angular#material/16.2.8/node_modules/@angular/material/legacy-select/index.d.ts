import { AnimationTriggerMetadata } from '@angular/animations';
import { ConnectedPosition } from '@angular/cdk/overlay';
import * as i0 from '@angular/core';
import * as i2 from '@angular/common';
import * as i3 from '@angular/cdk/overlay';
import * as i4 from '@angular/material/legacy-core';
import * as i5 from '@angular/material/core';
import * as i6 from '@angular/cdk/scrolling';
import * as i7 from '@angular/material/legacy-form-field';
import { MAT_SELECT_CONFIG as MAT_LEGACY_SELECT_CONFIG } from '@angular/material/select';
import { MAT_SELECT_SCROLL_STRATEGY as MAT_LEGACY_SELECT_SCROLL_STRATEGY } from '@angular/material/select';
import { MAT_SELECT_SCROLL_STRATEGY_PROVIDER as MAT_LEGACY_SELECT_SCROLL_STRATEGY_PROVIDER } from '@angular/material/select';
import { MAT_SELECT_SCROLL_STRATEGY_PROVIDER_FACTORY as MAT_LEGACY_SELECT_SCROLL_STRATEGY_PROVIDER_FACTORY } from '@angular/material/select';
import { MAT_SELECT_TRIGGER as MAT_LEGACY_SELECT_TRIGGER } from '@angular/material/select';
import { MatLegacyOptgroup } from '@angular/material/legacy-core';
import { MatLegacyOption } from '@angular/material/legacy-core';
import { _MatSelectBase } from '@angular/material/select';
import { MatSelectConfig } from '@angular/material/select';
import { OnInit } from '@angular/core';
import { QueryList } from '@angular/core';

declare namespace i1 {
    export {
        SELECT_PANEL_MAX_HEIGHT,
        SELECT_PANEL_PADDING_X,
        SELECT_PANEL_INDENT_PADDING_X,
        SELECT_ITEM_HEIGHT_EM,
        SELECT_MULTIPLE_PANEL_PADDING_X,
        SELECT_PANEL_VIEWPORT_PADDING,
        MatLegacySelectChange,
        MatLegacySelectConfig,
        MatLegacySelectTrigger,
        MatLegacySelect
    }
}

export { MAT_LEGACY_SELECT_CONFIG }

export { MAT_LEGACY_SELECT_SCROLL_STRATEGY }

export { MAT_LEGACY_SELECT_SCROLL_STRATEGY_PROVIDER }

export { MAT_LEGACY_SELECT_SCROLL_STRATEGY_PROVIDER_FACTORY }

export { MAT_LEGACY_SELECT_TRIGGER }

/**
 * @deprecated Use `MatSelect` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySelect extends _MatSelectBase<MatLegacySelectChange> implements OnInit {
    /** The scroll position of the overlay panel, calculated to center the selected option. */
    private _scrollTop;
    /** The last measured value for the trigger's client bounding rect. */
    private _triggerRect;
    /** The cached font-size of the trigger element. */
    _triggerFontSize: number;
    /** The value of the select panel's transform-origin property. */
    _transformOrigin: string;
    /**
     * The y-offset of the overlay panel in relation to the trigger's top start corner.
     * This must be adjusted to align the selected option text over the trigger text.
     * when the panel opens. Will change based on the y-position of the selected option.
     */
    _offsetY: number;
    options: QueryList<MatLegacyOption>;
    optionGroups: QueryList<MatLegacyOptgroup>;
    customTrigger: MatLegacySelectTrigger;
    _positions: ConnectedPosition[];
    /**
     * Calculates the scroll position of the select's overlay panel.
     *
     * Attempts to center the selected option in the panel. If the option is
     * too high or too low in the panel to be scrolled to the center, it clamps the
     * scroll position to the min or max scroll positions respectively.
     */
    _calculateOverlayScroll(selectedIndex: number, scrollBuffer: number, maxScroll: number): number;
    ngOnInit(): void;
    open(): void;
    /** Scrolls the active option into view. */
    protected _scrollOptionIntoView(index: number): void;
    protected _positioningSettled(): void;
    protected _panelDoneAnimating(isOpen: boolean): void;
    protected _getChangeEvent(value: any): MatLegacySelectChange;
    protected _getOverlayMinWidth(): number;
    /**
     * Sets the x-offset of the overlay panel in relation to the trigger's top start corner.
     * This must be adjusted to align the selected option text over the trigger text when
     * the panel opens. Will change based on LTR or RTL text direction. Note that the offset
     * can't be calculated until the panel has been attached, because we need to know the
     * content width in order to constrain the panel within the viewport.
     */
    private _calculateOverlayOffsetX;
    /**
     * Calculates the y-offset of the select's overlay panel in relation to the
     * top start corner of the trigger. It has to be adjusted in order for the
     * selected option to be aligned over the trigger when the panel opens.
     */
    private _calculateOverlayOffsetY;
    /**
     * Checks that the attempted overlay position will fit within the viewport.
     * If it will not fit, tries to adjust the scroll position and the associated
     * y-offset so the panel can open fully on-screen. If it still won't fit,
     * sets the offset back to 0 to allow the fallback position to take over.
     */
    private _checkOverlayWithinViewport;
    /** Adjusts the overlay panel up to fit in the viewport. */
    private _adjustPanelUp;
    /** Adjusts the overlay panel down to fit in the viewport. */
    private _adjustPanelDown;
    /** Calculates the scroll position and x- and y-offsets of the overlay panel. */
    private _calculateOverlayPosition;
    /** Sets the transform origin point based on the selected option. */
    private _getOriginBasedOnOption;
    /** Calculates the height of the select's options. */
    private _getItemHeight;
    /** Calculates the amount of items in the select. This includes options and group labels. */
    private _getItemCount;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySelect, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacySelect, "mat-select", ["matSelect"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, ["customTrigger", "options", "optionGroups"], ["mat-select-trigger", "*"], false, never>;
}

/**
 * The following are all the animations for the mat-select component, with each
 * const containing the metadata for one animation.
 *
 * The values below match the implementation of the AngularJS Material mat-select animation.
 * @docs-private
 * @deprecated Use `matSelectAnimations` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const matLegacySelectAnimations: {
    readonly transformPanelWrap: AnimationTriggerMetadata;
    readonly transformPanel: AnimationTriggerMetadata;
};

/**
 * Change event object that is emitted when the select value has changed.
 * @deprecated Use `MatSelectChange` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySelectChange {
    /** Reference to the select that emitted the change event. */
    source: MatLegacySelect;
    /** Current value of the select that emitted the event. */
    value: any;
    constructor(
    /** Reference to the select that emitted the change event. */
    source: MatLegacySelect, 
    /** Current value of the select that emitted the event. */
    value: any);
}

/**
 * @deprecated Use `MatSelectConfig` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare type MatLegacySelectConfig = Omit<MatSelectConfig, 'panelWidth'>;

/**
 * @deprecated Use `MatSelectModule` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySelectModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySelectModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacySelectModule, [typeof i1.MatLegacySelect, typeof i1.MatLegacySelectTrigger], [typeof i2.CommonModule, typeof i3.OverlayModule, typeof i4.MatLegacyOptionModule, typeof i5.MatCommonModule], [typeof i6.CdkScrollableModule, typeof i7.MatLegacyFormFieldModule, typeof i1.MatLegacySelect, typeof i1.MatLegacySelectTrigger, typeof i4.MatLegacyOptionModule, typeof i5.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacySelectModule>;
}

/**
 * Allows the user to customize the trigger that is displayed when the select has a value.
 * @deprecated Use `MatSelectTrigger` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySelectTrigger {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySelectTrigger, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacySelectTrigger, "mat-select-trigger", never, {}, {}, never, never, false, never>;
}

/**
 * The height of the select items in `em` units.
 * @deprecated Use `SELECT_ITEM_HEIGHT_EM` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
declare const SELECT_ITEM_HEIGHT_EM = 3;

/**
 * Distance between the panel edge and the option text in
 * multi-selection mode.
 *
 * Calculated as:
 * (SELECT_PANEL_PADDING_X * 1.5) + 16 = 40
 * The padding is multiplied by 1.5 because the checkbox's margin is half the padding.
 * The checkbox width is 16px.
 *
 * @deprecated Use `SELECT_MULTIPLE_PANEL_PADDING_X` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
declare const SELECT_MULTIPLE_PANEL_PADDING_X: number;

/**
 * The panel's x axis padding if it is indented (e.g. there is an option group).
 * @deprecated Use `SELECT_PANEL_INDENT_PADDING_X` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
declare const SELECT_PANEL_INDENT_PADDING_X: number;

/**
 * The max height of the select's overlay panel.
 * @deprecated Use `SELECT_PANEL_MAX_HEIGHT` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
declare const SELECT_PANEL_MAX_HEIGHT = 256;

/**
 * The panel's padding on the x-axis.
 * @deprecated Use `SELECT_PANEL_PADDING_X` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
declare const SELECT_PANEL_PADDING_X = 16;

/**
 * The select panel will only "fit" inside the viewport if it is positioned at
 * this value or more away from the viewport boundary.
 * @deprecated Use `SELECT_PANEL_VIEWPORT_PADDING` from `@angular/material/select` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
declare const SELECT_PANEL_VIEWPORT_PADDING = 8;

export { }
