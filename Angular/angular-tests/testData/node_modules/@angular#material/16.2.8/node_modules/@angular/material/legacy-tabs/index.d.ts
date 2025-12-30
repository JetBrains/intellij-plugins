import { CdkPortalOutlet } from '@angular/cdk/portal';
import { ChangeDetectorRef } from '@angular/core';
import { ComponentFactoryResolver } from '@angular/core';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i10 from '@angular/common';
import * as i11 from '@angular/material/core';
import * as i12 from '@angular/cdk/portal';
import * as i13 from '@angular/cdk/observers';
import * as i14 from '@angular/cdk/a11y';
import { ScrollDirection as LegacyScrollDirection } from '@angular/material/tabs';
import { _MAT_INK_BAR_POSITIONER as _MAT_LEGACY_INK_BAR_POSITIONER } from '@angular/material/tabs';
import { _MAT_INK_BAR_POSITIONER_FACTORY as _MAT_LEGACY_INK_BAR_POSITIONER_FACTORY } from '@angular/material/tabs';
import { MAT_TAB as MAT_LEGACY_TAB } from '@angular/material/tabs';
import { MAT_TAB_CONTENT as MAT_LEGACY_TAB_CONTENT } from '@angular/material/tabs';
import { MAT_TAB_GROUP as MAT_LEGACY_TAB_GROUP } from '@angular/material/tabs';
import { MAT_TABS_CONFIG as MAT_LEGACY_TABS_CONFIG } from '@angular/material/tabs';
import { _MatInkBarPositioner as _MatLegacyInkBarPositioner } from '@angular/material/tabs';
import { MatPaginatedTabHeader as MatLegacyPaginatedTabHeader } from '@angular/material/tabs';
import { _MatTabBase as _MatLegacyTabBase } from '@angular/material/tabs';
import { _MatTabBodyBase as _MatLegacyTabBodyBase } from '@angular/material/tabs';
import { MatTabBodyOriginState as MatLegacyTabBodyOriginState } from '@angular/material/tabs';
import { MatTabBodyPositionState as MatLegacyTabBodyPositionState } from '@angular/material/tabs';
import { MatTabChangeEvent as MatLegacyTabChangeEvent } from '@angular/material/tabs';
import { _MatTabGroupBase as _MatLegacyTabGroupBase } from '@angular/material/tabs';
import { _MatTabHeaderBase as _MatLegacyTabHeaderBase } from '@angular/material/tabs';
import { MatTabHeaderPosition as MatLegacyTabHeaderPosition } from '@angular/material/tabs';
import { _MatTabLinkBase as _MatLegacyTabLinkBase } from '@angular/material/tabs';
import { _MatTabNavBase as _MatLegacyTabNavBase } from '@angular/material/tabs';
import { matTabsAnimations as matLegacyTabsAnimations } from '@angular/material/tabs';
import { MatTabsConfig as MatLegacyTabsConfig } from '@angular/material/tabs';
import { MatTabBodyPortal } from '@angular/material/tabs';
import { MatTabContent } from '@angular/material/tabs';
import { MatTabGroupBaseHeader } from '@angular/material/tabs';
import { MatTabLabel } from '@angular/material/tabs';
import { _MatTabLabelWrapperBase } from '@angular/material/tabs';
import { NgZone } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { QueryList } from '@angular/core';
import { RippleGlobalOptions } from '@angular/material/core';
import { TemplateRef } from '@angular/core';
import { ViewContainerRef } from '@angular/core';
import { ViewportRuler } from '@angular/cdk/scrolling';

declare namespace i1 {
    export {
        MatLegacyTabGroup
    }
}

declare namespace i2 {
    export {
        MatLegacyTabLabel
    }
}

declare namespace i3 {
    export {
        MatLegacyTab
    }
}

declare namespace i4 {
    export {
        MatLegacyInkBar
    }
}

declare namespace i5 {
    export {
        MatLegacyTabLabelWrapper
    }
}

declare namespace i6 {
    export {
        MatLegacyTabNav,
        MatLegacyTabLink,
        MatLegacyTabNavPanel
    }
}

declare namespace i7 {
    export {
        MatLegacyTabBodyPortal,
        MatLegacyTabBody
    }
}

declare namespace i8 {
    export {
        MatLegacyTabHeader
    }
}

declare namespace i9 {
    export {
        MatLegacyTabContent
    }
}

export { LegacyScrollDirection }

export { _MAT_LEGACY_INK_BAR_POSITIONER }

export { _MAT_LEGACY_INK_BAR_POSITIONER_FACTORY }

export { MAT_LEGACY_TAB }

export { MAT_LEGACY_TAB_CONTENT }

export { MAT_LEGACY_TAB_GROUP }

export { MAT_LEGACY_TABS_CONFIG }

/**
 * The ink-bar is used to display and animate the line underneath the current active tab label.
 * @docs-private
 * @deprecated Use `MatInkBar` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyInkBar {
    private _elementRef;
    private _ngZone;
    private _inkBarPositioner;
    _animationMode?: string | undefined;
    constructor(_elementRef: ElementRef<HTMLElement>, _ngZone: NgZone, _inkBarPositioner: _MatLegacyInkBarPositioner, _animationMode?: string | undefined);
    /**
     * Calculates the styles from the provided element in order to align the ink-bar to that element.
     * Shows the ink bar if previously set as hidden.
     * @param element
     */
    alignToElement(element: HTMLElement): void;
    /** Shows the ink bar. */
    show(): void;
    /** Hides the ink bar. */
    hide(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyInkBar, [null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyInkBar, "mat-ink-bar", never, {}, {}, never, never, false, never>;
}

export { _MatLegacyInkBarPositioner }

export { MatLegacyPaginatedTabHeader }

/**
 * @deprecated Use `MatTab` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTab extends _MatLegacyTabBase {
    /** Content for the tab label given by `<ng-template mat-tab-label>`. */
    get templateLabel(): MatTabLabel;
    set templateLabel(value: MatTabLabel);
    /**
     * Template provided in the tab content that will be used if present, used to enable lazy-loading
     */
    _explicitContent: TemplateRef<any>;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTab, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyTab, "mat-tab", ["matTab"], { "disabled": { "alias": "disabled"; "required": false; }; }, {}, ["templateLabel", "_explicitContent"], ["*"], false, never>;
}

export { _MatLegacyTabBase }

/**
 * Wrapper for the contents of a tab.
 * @docs-private
 * @deprecated Use `MatTabBody` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabBody extends _MatLegacyTabBodyBase {
    _portalHost: CdkPortalOutlet;
    constructor(elementRef: ElementRef<HTMLElement>, dir: Directionality, changeDetectorRef: ChangeDetectorRef);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabBody, [null, { optional: true; }, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyTabBody, "mat-tab-body", never, {}, {}, never, never, false, never>;
}

export { _MatLegacyTabBodyBase }

export { MatLegacyTabBodyOriginState }

/**
 * The portal host directive for the contents of the tab.
 * @docs-private
 * @deprecated Use `MatTabBodyPortal` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabBodyPortal extends MatTabBodyPortal {
    constructor(componentFactoryResolver: ComponentFactoryResolver, viewContainerRef: ViewContainerRef, host: MatLegacyTabBody, _document: any);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabBodyPortal, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyTabBodyPortal, "[matTabBodyHost]", never, {}, {}, never, never, false, never>;
}

export { MatLegacyTabBodyPositionState }

export { MatLegacyTabChangeEvent }

/**
 * Decorates the `ng-template` tags and reads out the template from it.
 * @deprecated Use `MatTabContent` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabContent extends MatTabContent {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabContent, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyTabContent, "[matTabContent]", never, {}, {}, never, never, false, never>;
}

/**
 * Material design tab-group component. Supports basic tab pairs (label + content) and includes
 * animated ink-bar, keyboard navigation, and screen reader.
 * See: https://material.io/design/components/tabs.html
 * @deprecated Use `MatTabGroup` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabGroup extends _MatLegacyTabGroupBase {
    _allTabs: QueryList<MatLegacyTab>;
    _tabBodyWrapper: ElementRef;
    _tabHeader: MatTabGroupBaseHeader;
    constructor(elementRef: ElementRef, changeDetectorRef: ChangeDetectorRef, defaultConfig?: MatLegacyTabsConfig, animationMode?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabGroup, [null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyTabGroup, "mat-tab-group", ["matTabGroup"], { "color": { "alias": "color"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; }, {}, ["_allTabs"], never, false, never>;
}

export { _MatLegacyTabGroupBase }

/**
 * The header of the tab group which displays a list of all the tabs in the tab group. Includes
 * an ink bar that follows the currently selected tab. When the tabs list's width exceeds the
 * width of the header container, then arrows will be displayed to allow the user to scroll
 * left and right across the header.
 * @docs-private
 * @deprecated Use `MatTabHeader` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabHeader extends _MatLegacyTabHeaderBase {
    _items: QueryList<MatLegacyTabLabelWrapper>;
    _inkBar: MatLegacyInkBar;
    _tabListContainer: ElementRef;
    _tabList: ElementRef;
    _tabListInner: ElementRef;
    _nextPaginator: ElementRef<HTMLElement>;
    _previousPaginator: ElementRef<HTMLElement>;
    constructor(elementRef: ElementRef, changeDetectorRef: ChangeDetectorRef, viewportRuler: ViewportRuler, dir: Directionality, ngZone: NgZone, platform: Platform, animationMode?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabHeader, [null, null, null, { optional: true; }, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyTabHeader, "mat-tab-header", never, { "selectedIndex": { "alias": "selectedIndex"; "required": false; }; }, { "selectFocusedIndex": "selectFocusedIndex"; "indexFocused": "indexFocused"; }, ["_items"], ["*"], false, never>;
}

export { _MatLegacyTabHeaderBase }

export { MatLegacyTabHeaderPosition }

/**
 * Used to flag tab labels for use with the portal directive
 * @deprecated Use `MatTabLabel` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabLabel extends MatTabLabel {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabLabel, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyTabLabel, "[mat-tab-label], [matTabLabel]", never, {}, {}, never, never, false, never>;
}

/**
 * @deprecated Use `MatTabLabelWrapper` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabLabelWrapper extends _MatTabLabelWrapperBase {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabLabelWrapper, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyTabLabelWrapper, "[matTabLabelWrapper]", never, { "disabled": { "alias": "disabled"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * Link inside of a `mat-tab-nav-bar`.
 * @deprecated Use `MatTabLink` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabLink extends _MatLegacyTabLinkBase implements OnDestroy {
    /** Reference to the RippleRenderer for the tab-link. */
    private _tabLinkRipple;
    constructor(tabNavBar: MatLegacyTabNav, elementRef: ElementRef, ngZone: NgZone, platform: Platform, globalRippleOptions: RippleGlobalOptions | null, tabIndex: string, focusMonitor: FocusMonitor, animationMode?: string);
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabLink, [null, null, null, null, { optional: true; }, { attribute: "tabindex"; }, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyTabLink, "[mat-tab-link], [matTabLink]", ["matTabLink"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, never, false, never>;
}

export { _MatLegacyTabLinkBase }

/**
 * Navigation component matching the styles of the tab group header.
 * Provides anchored navigation with animated ink bar.
 * @deprecated Use `MatTabNav` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabNav extends _MatLegacyTabNavBase {
    _items: QueryList<MatLegacyTabLink>;
    _inkBar: MatLegacyInkBar;
    _tabListContainer: ElementRef;
    _tabList: ElementRef;
    _tabListInner: ElementRef;
    _nextPaginator: ElementRef<HTMLElement>;
    _previousPaginator: ElementRef<HTMLElement>;
    constructor(elementRef: ElementRef, dir: Directionality, ngZone: NgZone, changeDetectorRef: ChangeDetectorRef, viewportRuler: ViewportRuler, platform: Platform, animationMode?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabNav, [null, { optional: true; }, null, null, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyTabNav, "[mat-tab-nav-bar]", ["matTabNavBar", "matTabNav"], { "color": { "alias": "color"; "required": false; }; }, {}, ["_items"], ["*"], false, never>;
}

export { _MatLegacyTabNavBase }

/**
 * Tab panel component associated with MatTabNav.
 * @deprecated Use `MatTabNavPanel` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabNavPanel {
    /** Unique id for the tab panel. */
    id: string;
    /** Id of the active tab in the nav bar. */
    _activeTabId?: string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabNavPanel, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyTabNavPanel, "mat-tab-nav-panel", ["matTabNavPanel"], { "id": { "alias": "id"; "required": false; }; }, {}, never, ["*"], false, never>;
}

export { matLegacyTabsAnimations }

export { MatLegacyTabsConfig }

/**
 * @deprecated Use `MatTabsModule` from `@angular/material/tabs` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTabsModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTabsModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyTabsModule, [typeof i1.MatLegacyTabGroup, typeof i2.MatLegacyTabLabel, typeof i3.MatLegacyTab, typeof i4.MatLegacyInkBar, typeof i5.MatLegacyTabLabelWrapper, typeof i6.MatLegacyTabNav, typeof i6.MatLegacyTabNavPanel, typeof i6.MatLegacyTabLink, typeof i7.MatLegacyTabBody, typeof i7.MatLegacyTabBodyPortal, typeof i8.MatLegacyTabHeader, typeof i9.MatLegacyTabContent], [typeof i10.CommonModule, typeof i11.MatCommonModule, typeof i12.PortalModule, typeof i11.MatRippleModule, typeof i13.ObserversModule, typeof i14.A11yModule], [typeof i11.MatCommonModule, typeof i1.MatLegacyTabGroup, typeof i2.MatLegacyTabLabel, typeof i3.MatLegacyTab, typeof i6.MatLegacyTabNav, typeof i6.MatLegacyTabNavPanel, typeof i6.MatLegacyTabLink, typeof i9.MatLegacyTabContent]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyTabsModule>;
}

export { }
