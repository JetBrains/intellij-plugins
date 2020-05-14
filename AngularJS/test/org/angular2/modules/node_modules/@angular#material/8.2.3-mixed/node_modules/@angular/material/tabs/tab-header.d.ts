/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { Directionality } from '@angular/cdk/bidi';
import { ViewportRuler } from '@angular/cdk/scrolling';
import { AfterContentChecked, AfterContentInit, ChangeDetectorRef, ElementRef, NgZone, OnDestroy, QueryList, AfterViewInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { MatInkBar } from './ink-bar';
import { MatTabLabelWrapper } from './tab-label-wrapper';
import { Platform } from '@angular/cdk/platform';
import { MatPaginatedTabHeader } from './paginated-tab-header';
/**
 * Base class with all of the `MatTabHeader` functionality.
 * @docs-private
 */
import * as ɵngcc0 from '@angular/core';
export declare abstract class _MatTabHeaderBase extends MatPaginatedTabHeader implements AfterContentChecked, AfterContentInit, AfterViewInit, OnDestroy {
    /** Whether the ripple effect is disabled or not. */
    get disableRipple(): any;
    set disableRipple(value: any);
    private _disableRipple;
    constructor(elementRef: ElementRef, changeDetectorRef: ChangeDetectorRef, viewportRuler: ViewportRuler, dir: Directionality, ngZone: NgZone, platform: Platform, animationMode?: string);
    protected _itemSelected(event: KeyboardEvent): void;
    static ɵfac: ɵngcc0.ɵɵFactoryDef<_MatTabHeaderBase, [null, null, null, { optional: true; }, null, null, { optional: true; }]>;
    static ɵdir: ɵngcc0.ɵɵDirectiveDefWithMeta<_MatTabHeaderBase, never, never, { "disableRipple": "disableRipple"; }, {}, never>;
}
/**
 * The header of the tab group which displays a list of all the tabs in the tab group. Includes
 * an ink bar that follows the currently selected tab. When the tabs list's width exceeds the
 * width of the header container, then arrows will be displayed to allow the user to scroll
 * left and right across the header.
 * @docs-private
 */
export declare class MatTabHeader extends _MatTabHeaderBase {
    _items: QueryList<MatTabLabelWrapper>;
    _inkBar: MatInkBar;
    _tabListContainer: ElementRef;
    _tabList: ElementRef;
    _nextPaginator: ElementRef<HTMLElement>;
    _previousPaginator: ElementRef<HTMLElement>;
    constructor(elementRef: ElementRef, changeDetectorRef: ChangeDetectorRef, viewportRuler: ViewportRuler, dir: Directionality, ngZone: NgZone, platform: Platform, animationMode?: string);
    static ngAcceptInputType_disableRipple: BooleanInput;
    static ɵfac: ɵngcc0.ɵɵFactoryDef<MatTabHeader, [null, null, null, { optional: true; }, null, null, { optional: true; }]>;
    static ɵcmp: ɵngcc0.ɵɵComponentDefWithMeta<MatTabHeader, "mat-tab-header", never, { "selectedIndex": "selectedIndex"; }, { "selectFocusedIndex": "selectFocusedIndex"; "indexFocused": "indexFocused"; }, ["_items"], ["*"]>;
}

//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoidGFiLWhlYWRlci5kLnRzIiwic291cmNlcyI6WyJ0YWItaGVhZGVyLmQudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQUNBIiwic291cmNlc0NvbnRlbnQiOlsiLyoqXG4gKiBAbGljZW5zZVxuICogQ29weXJpZ2h0IEdvb2dsZSBMTEMgQWxsIFJpZ2h0cyBSZXNlcnZlZC5cbiAqXG4gKiBVc2Ugb2YgdGhpcyBzb3VyY2UgY29kZSBpcyBnb3Zlcm5lZCBieSBhbiBNSVQtc3R5bGUgbGljZW5zZSB0aGF0IGNhbiBiZVxuICogZm91bmQgaW4gdGhlIExJQ0VOU0UgZmlsZSBhdCBodHRwczovL2FuZ3VsYXIuaW8vbGljZW5zZVxuICovXG5pbXBvcnQgeyBEaXJlY3Rpb25hbGl0eSB9IGZyb20gJ0Bhbmd1bGFyL2Nkay9iaWRpJztcbmltcG9ydCB7IFZpZXdwb3J0UnVsZXIgfSBmcm9tICdAYW5ndWxhci9jZGsvc2Nyb2xsaW5nJztcbmltcG9ydCB7IEFmdGVyQ29udGVudENoZWNrZWQsIEFmdGVyQ29udGVudEluaXQsIENoYW5nZURldGVjdG9yUmVmLCBFbGVtZW50UmVmLCBOZ1pvbmUsIE9uRGVzdHJveSwgUXVlcnlMaXN0LCBBZnRlclZpZXdJbml0IH0gZnJvbSAnQGFuZ3VsYXIvY29yZSc7XG5pbXBvcnQgeyBCb29sZWFuSW5wdXQgfSBmcm9tICdAYW5ndWxhci9jZGsvY29lcmNpb24nO1xuaW1wb3J0IHsgTWF0SW5rQmFyIH0gZnJvbSAnLi9pbmstYmFyJztcbmltcG9ydCB7IE1hdFRhYkxhYmVsV3JhcHBlciB9IGZyb20gJy4vdGFiLWxhYmVsLXdyYXBwZXInO1xuaW1wb3J0IHsgUGxhdGZvcm0gfSBmcm9tICdAYW5ndWxhci9jZGsvcGxhdGZvcm0nO1xuaW1wb3J0IHsgTWF0UGFnaW5hdGVkVGFiSGVhZGVyIH0gZnJvbSAnLi9wYWdpbmF0ZWQtdGFiLWhlYWRlcic7XG4vKipcbiAqIEJhc2UgY2xhc3Mgd2l0aCBhbGwgb2YgdGhlIGBNYXRUYWJIZWFkZXJgIGZ1bmN0aW9uYWxpdHkuXG4gKiBAZG9jcy1wcml2YXRlXG4gKi9cbmV4cG9ydCBkZWNsYXJlIGFic3RyYWN0IGNsYXNzIF9NYXRUYWJIZWFkZXJCYXNlIGV4dGVuZHMgTWF0UGFnaW5hdGVkVGFiSGVhZGVyIGltcGxlbWVudHMgQWZ0ZXJDb250ZW50Q2hlY2tlZCwgQWZ0ZXJDb250ZW50SW5pdCwgQWZ0ZXJWaWV3SW5pdCwgT25EZXN0cm95IHtcbiAgICAvKiogV2hldGhlciB0aGUgcmlwcGxlIGVmZmVjdCBpcyBkaXNhYmxlZCBvciBub3QuICovXG4gICAgZ2V0IGRpc2FibGVSaXBwbGUoKTogYW55O1xuICAgIHNldCBkaXNhYmxlUmlwcGxlKHZhbHVlOiBhbnkpO1xuICAgIHByaXZhdGUgX2Rpc2FibGVSaXBwbGU7XG4gICAgY29uc3RydWN0b3IoZWxlbWVudFJlZjogRWxlbWVudFJlZiwgY2hhbmdlRGV0ZWN0b3JSZWY6IENoYW5nZURldGVjdG9yUmVmLCB2aWV3cG9ydFJ1bGVyOiBWaWV3cG9ydFJ1bGVyLCBkaXI6IERpcmVjdGlvbmFsaXR5LCBuZ1pvbmU6IE5nWm9uZSwgcGxhdGZvcm06IFBsYXRmb3JtLCBhbmltYXRpb25Nb2RlPzogc3RyaW5nKTtcbiAgICBwcm90ZWN0ZWQgX2l0ZW1TZWxlY3RlZChldmVudDogS2V5Ym9hcmRFdmVudCk6IHZvaWQ7XG59XG4vKipcbiAqIFRoZSBoZWFkZXIgb2YgdGhlIHRhYiBncm91cCB3aGljaCBkaXNwbGF5cyBhIGxpc3Qgb2YgYWxsIHRoZSB0YWJzIGluIHRoZSB0YWIgZ3JvdXAuIEluY2x1ZGVzXG4gKiBhbiBpbmsgYmFyIHRoYXQgZm9sbG93cyB0aGUgY3VycmVudGx5IHNlbGVjdGVkIHRhYi4gV2hlbiB0aGUgdGFicyBsaXN0J3Mgd2lkdGggZXhjZWVkcyB0aGVcbiAqIHdpZHRoIG9mIHRoZSBoZWFkZXIgY29udGFpbmVyLCB0aGVuIGFycm93cyB3aWxsIGJlIGRpc3BsYXllZCB0byBhbGxvdyB0aGUgdXNlciB0byBzY3JvbGxcbiAqIGxlZnQgYW5kIHJpZ2h0IGFjcm9zcyB0aGUgaGVhZGVyLlxuICogQGRvY3MtcHJpdmF0ZVxuICovXG5leHBvcnQgZGVjbGFyZSBjbGFzcyBNYXRUYWJIZWFkZXIgZXh0ZW5kcyBfTWF0VGFiSGVhZGVyQmFzZSB7XG4gICAgX2l0ZW1zOiBRdWVyeUxpc3Q8TWF0VGFiTGFiZWxXcmFwcGVyPjtcbiAgICBfaW5rQmFyOiBNYXRJbmtCYXI7XG4gICAgX3RhYkxpc3RDb250YWluZXI6IEVsZW1lbnRSZWY7XG4gICAgX3RhYkxpc3Q6IEVsZW1lbnRSZWY7XG4gICAgX25leHRQYWdpbmF0b3I6IEVsZW1lbnRSZWY8SFRNTEVsZW1lbnQ+O1xuICAgIF9wcmV2aW91c1BhZ2luYXRvcjogRWxlbWVudFJlZjxIVE1MRWxlbWVudD47XG4gICAgY29uc3RydWN0b3IoZWxlbWVudFJlZjogRWxlbWVudFJlZiwgY2hhbmdlRGV0ZWN0b3JSZWY6IENoYW5nZURldGVjdG9yUmVmLCB2aWV3cG9ydFJ1bGVyOiBWaWV3cG9ydFJ1bGVyLCBkaXI6IERpcmVjdGlvbmFsaXR5LCBuZ1pvbmU6IE5nWm9uZSwgcGxhdGZvcm06IFBsYXRmb3JtLCBhbmltYXRpb25Nb2RlPzogc3RyaW5nKTtcbiAgICBzdGF0aWMgbmdBY2NlcHRJbnB1dFR5cGVfZGlzYWJsZVJpcHBsZTogQm9vbGVhbklucHV0O1xufVxuIl19