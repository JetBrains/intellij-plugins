/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ChangeDetectorRef, ElementRef, NgZone, QueryList, EventEmitter, AfterContentChecked, AfterContentInit, AfterViewInit, OnDestroy } from '@angular/core';
import { Direction, Directionality } from '@angular/cdk/bidi';
import { NumberInput } from '@angular/cdk/coercion';
import { ViewportRuler } from '@angular/cdk/scrolling';
import { FocusableOption } from '@angular/cdk/a11y';
import { Subject } from 'rxjs';
import { Platform } from '@angular/cdk/platform';
/**
 * The directions that scrolling can go in when the header's tabs exceed the header width. 'After'
 * will scroll the header towards the end of the tabs list and 'before' will scroll towards the
 * beginning of the list.
 */
import * as ɵngcc0 from '@angular/core';
export declare type ScrollDirection = 'after' | 'before';
/** Item inside a paginated tab header. */
export declare type MatPaginatedTabHeaderItem = FocusableOption & {
    elementRef: ElementRef;
};
/**
 * Base class for a tab header that supported pagination.
 * @docs-private
 */
export declare abstract class MatPaginatedTabHeader implements AfterContentChecked, AfterContentInit, AfterViewInit, OnDestroy {
    protected _elementRef: ElementRef<HTMLElement>;
    protected _changeDetectorRef: ChangeDetectorRef;
    private _viewportRuler;
    private _dir;
    private _ngZone;
    /**
     * @deprecated @breaking-change 9.0.0 `_platform` and `_animationMode`
     * parameters to become required.
     */
    private _platform?;
    _animationMode?: string | undefined;
    abstract _items: QueryList<MatPaginatedTabHeaderItem>;
    abstract _inkBar: {
        hide: () => void;
        alignToElement: (element: HTMLElement) => void;
    };
    abstract _tabListContainer: ElementRef<HTMLElement>;
    abstract _tabList: ElementRef<HTMLElement>;
    abstract _nextPaginator: ElementRef<HTMLElement>;
    abstract _previousPaginator: ElementRef<HTMLElement>;
    /** The distance in pixels that the tab labels should be translated to the left. */
    private _scrollDistance;
    /** Whether the header should scroll to the selected index after the view has been checked. */
    private _selectedIndexChanged;
    /** Emits when the component is destroyed. */
    protected readonly _destroyed: Subject<void>;
    /** Whether the controls for pagination should be displayed */
    _showPaginationControls: boolean;
    /** Whether the tab list can be scrolled more towards the end of the tab label list. */
    _disableScrollAfter: boolean;
    /** Whether the tab list can be scrolled more towards the beginning of the tab label list. */
    _disableScrollBefore: boolean;
    /**
     * The number of tab labels that are displayed on the header. When this changes, the header
     * should re-evaluate the scroll position.
     */
    private _tabLabelCount;
    /** Whether the scroll distance has changed and should be applied after the view is checked. */
    private _scrollDistanceChanged;
    /** Used to manage focus between the tabs. */
    private _keyManager;
    /** Cached text content of the header. */
    private _currentTextContent;
    /** Stream that will stop the automated scrolling. */
    private _stopScrolling;
    /**
     * Whether pagination should be disabled. This can be used to avoid unnecessary
     * layout recalculations if it's known that pagination won't be required.
     */
    disablePagination: boolean;
    /** The index of the active tab. */
    get selectedIndex(): number;
    set selectedIndex(value: number);
    private _selectedIndex;
    /** Event emitted when the option is selected. */
    readonly selectFocusedIndex: EventEmitter<number>;
    /** Event emitted when a label is focused. */
    readonly indexFocused: EventEmitter<number>;
    constructor(_elementRef: ElementRef<HTMLElement>, _changeDetectorRef: ChangeDetectorRef, _viewportRuler: ViewportRuler, _dir: Directionality, _ngZone: NgZone, 
    /**
     * @deprecated @breaking-change 9.0.0 `_platform` and `_animationMode`
     * parameters to become required.
     */
    _platform?: Platform | undefined, _animationMode?: string | undefined);
    /** Called when the user has selected an item via the keyboard. */
    protected abstract _itemSelected(event: KeyboardEvent): void;
    ngAfterViewInit(): void;
    ngAfterContentInit(): void;
    ngAfterContentChecked(): void;
    ngOnDestroy(): void;
    /** Handles keyboard events on the header. */
    _handleKeydown(event: KeyboardEvent): void;
    /**
     * Callback for when the MutationObserver detects that the content has changed.
     */
    _onContentChanges(): void;
    /**
     * Updates the view whether pagination should be enabled or not.
     *
     * WARNING: Calling this method can be very costly in terms of performance. It should be called
     * as infrequently as possible from outside of the Tabs component as it causes a reflow of the
     * page.
     */
    updatePagination(): void;
    /** Tracks which element has focus; used for keyboard navigation */
    get focusIndex(): number;
    /** When the focus index is set, we must manually send focus to the correct label */
    set focusIndex(value: number);
    /**
     * Determines if an index is valid.  If the tabs are not ready yet, we assume that the user is
     * providing a valid index and return true.
     */
    _isValidIndex(index: number): boolean;
    /**
     * Sets focus on the HTML element for the label wrapper and scrolls it into the view if
     * scrolling is enabled.
     */
    _setTabFocus(tabIndex: number): void;
    /** The layout direction of the containing app. */
    _getLayoutDirection(): Direction;
    /** Performs the CSS transformation on the tab list that will cause the list to scroll. */
    _updateTabScrollPosition(): void;
    /** Sets the distance in pixels that the tab header should be transformed in the X-axis. */
    get scrollDistance(): number;
    set scrollDistance(value: number);
    /**
     * Moves the tab list in the 'before' or 'after' direction (towards the beginning of the list or
     * the end of the list, respectively). The distance to scroll is computed to be a third of the
     * length of the tab list view window.
     *
     * This is an expensive call that forces a layout reflow to compute box and scroll metrics and
     * should be called sparingly.
     */
    _scrollHeader(direction: ScrollDirection): {
        maxScrollDistance: number;
        distance: number;
    };
    /** Handles click events on the pagination arrows. */
    _handlePaginatorClick(direction: ScrollDirection): void;
    /**
     * Moves the tab list such that the desired tab label (marked by index) is moved into view.
     *
     * This is an expensive call that forces a layout reflow to compute box and scroll metrics and
     * should be called sparingly.
     */
    _scrollToLabel(labelIndex: number): void;
    /**
     * Evaluate whether the pagination controls should be displayed. If the scroll width of the
     * tab list is wider than the size of the header container, then the pagination controls should
     * be shown.
     *
     * This is an expensive call that forces a layout reflow to compute box and scroll metrics and
     * should be called sparingly.
     */
    _checkPaginationEnabled(): void;
    /**
     * Evaluate whether the before and after controls should be enabled or disabled.
     * If the header is at the beginning of the list (scroll distance is equal to 0) then disable the
     * before button. If the header is at the end of the list (scroll distance is equal to the
     * maximum distance we can scroll), then disable the after button.
     *
     * This is an expensive call that forces a layout reflow to compute box and scroll metrics and
     * should be called sparingly.
     */
    _checkScrollingControls(): void;
    /**
     * Determines what is the maximum length in pixels that can be set for the scroll distance. This
     * is equal to the difference in width between the tab list container and tab header container.
     *
     * This is an expensive call that forces a layout reflow to compute box and scroll metrics and
     * should be called sparingly.
     */
    _getMaxScrollDistance(): number;
    /** Tells the ink-bar to align itself to the current label wrapper */
    _alignInkBarToSelectedTab(): void;
    /** Stops the currently-running paginator interval.  */
    _stopInterval(): void;
    /**
     * Handles the user pressing down on one of the paginators.
     * Starts scrolling the header after a certain amount of time.
     * @param direction In which direction the paginator should be scrolled.
     */
    _handlePaginatorPress(direction: ScrollDirection, mouseEvent?: MouseEvent): void;
    /**
     * Scrolls the header to a given position.
     * @param position Position to which to scroll.
     * @returns Information on the current scroll distance and the maximum.
     */
    private _scrollTo;
    static ngAcceptInputType_selectedIndex: NumberInput;
    static ɵfac: ɵngcc0.ɵɵFactoryDef<MatPaginatedTabHeader, [null, null, null, { optional: true; }, null, null, { optional: true; }]>;
    static ɵdir: ɵngcc0.ɵɵDirectiveDefWithMeta<MatPaginatedTabHeader, never, never, { "disablePagination": "disablePagination"; }, {}, never>;
}

//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicGFnaW5hdGVkLXRhYi1oZWFkZXIuZC50cyIsInNvdXJjZXMiOlsicGFnaW5hdGVkLXRhYi1oZWFkZXIuZC50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQUNBIiwic291cmNlc0NvbnRlbnQiOlsiLyoqXG4gKiBAbGljZW5zZVxuICogQ29weXJpZ2h0IEdvb2dsZSBMTEMgQWxsIFJpZ2h0cyBSZXNlcnZlZC5cbiAqXG4gKiBVc2Ugb2YgdGhpcyBzb3VyY2UgY29kZSBpcyBnb3Zlcm5lZCBieSBhbiBNSVQtc3R5bGUgbGljZW5zZSB0aGF0IGNhbiBiZVxuICogZm91bmQgaW4gdGhlIExJQ0VOU0UgZmlsZSBhdCBodHRwczovL2FuZ3VsYXIuaW8vbGljZW5zZVxuICovXG5pbXBvcnQgeyBDaGFuZ2VEZXRlY3RvclJlZiwgRWxlbWVudFJlZiwgTmdab25lLCBRdWVyeUxpc3QsIEV2ZW50RW1pdHRlciwgQWZ0ZXJDb250ZW50Q2hlY2tlZCwgQWZ0ZXJDb250ZW50SW5pdCwgQWZ0ZXJWaWV3SW5pdCwgT25EZXN0cm95IH0gZnJvbSAnQGFuZ3VsYXIvY29yZSc7XG5pbXBvcnQgeyBEaXJlY3Rpb24sIERpcmVjdGlvbmFsaXR5IH0gZnJvbSAnQGFuZ3VsYXIvY2RrL2JpZGknO1xuaW1wb3J0IHsgTnVtYmVySW5wdXQgfSBmcm9tICdAYW5ndWxhci9jZGsvY29lcmNpb24nO1xuaW1wb3J0IHsgVmlld3BvcnRSdWxlciB9IGZyb20gJ0Bhbmd1bGFyL2Nkay9zY3JvbGxpbmcnO1xuaW1wb3J0IHsgRm9jdXNhYmxlT3B0aW9uIH0gZnJvbSAnQGFuZ3VsYXIvY2RrL2ExMXknO1xuaW1wb3J0IHsgU3ViamVjdCB9IGZyb20gJ3J4anMnO1xuaW1wb3J0IHsgUGxhdGZvcm0gfSBmcm9tICdAYW5ndWxhci9jZGsvcGxhdGZvcm0nO1xuLyoqXG4gKiBUaGUgZGlyZWN0aW9ucyB0aGF0IHNjcm9sbGluZyBjYW4gZ28gaW4gd2hlbiB0aGUgaGVhZGVyJ3MgdGFicyBleGNlZWQgdGhlIGhlYWRlciB3aWR0aC4gJ0FmdGVyJ1xuICogd2lsbCBzY3JvbGwgdGhlIGhlYWRlciB0b3dhcmRzIHRoZSBlbmQgb2YgdGhlIHRhYnMgbGlzdCBhbmQgJ2JlZm9yZScgd2lsbCBzY3JvbGwgdG93YXJkcyB0aGVcbiAqIGJlZ2lubmluZyBvZiB0aGUgbGlzdC5cbiAqL1xuZXhwb3J0IGRlY2xhcmUgdHlwZSBTY3JvbGxEaXJlY3Rpb24gPSAnYWZ0ZXInIHwgJ2JlZm9yZSc7XG4vKiogSXRlbSBpbnNpZGUgYSBwYWdpbmF0ZWQgdGFiIGhlYWRlci4gKi9cbmV4cG9ydCBkZWNsYXJlIHR5cGUgTWF0UGFnaW5hdGVkVGFiSGVhZGVySXRlbSA9IEZvY3VzYWJsZU9wdGlvbiAmIHtcbiAgICBlbGVtZW50UmVmOiBFbGVtZW50UmVmO1xufTtcbi8qKlxuICogQmFzZSBjbGFzcyBmb3IgYSB0YWIgaGVhZGVyIHRoYXQgc3VwcG9ydGVkIHBhZ2luYXRpb24uXG4gKiBAZG9jcy1wcml2YXRlXG4gKi9cbmV4cG9ydCBkZWNsYXJlIGFic3RyYWN0IGNsYXNzIE1hdFBhZ2luYXRlZFRhYkhlYWRlciBpbXBsZW1lbnRzIEFmdGVyQ29udGVudENoZWNrZWQsIEFmdGVyQ29udGVudEluaXQsIEFmdGVyVmlld0luaXQsIE9uRGVzdHJveSB7XG4gICAgcHJvdGVjdGVkIF9lbGVtZW50UmVmOiBFbGVtZW50UmVmPEhUTUxFbGVtZW50PjtcbiAgICBwcm90ZWN0ZWQgX2NoYW5nZURldGVjdG9yUmVmOiBDaGFuZ2VEZXRlY3RvclJlZjtcbiAgICBwcml2YXRlIF92aWV3cG9ydFJ1bGVyO1xuICAgIHByaXZhdGUgX2RpcjtcbiAgICBwcml2YXRlIF9uZ1pvbmU7XG4gICAgLyoqXG4gICAgICogQGRlcHJlY2F0ZWQgQGJyZWFraW5nLWNoYW5nZSA5LjAuMCBgX3BsYXRmb3JtYCBhbmQgYF9hbmltYXRpb25Nb2RlYFxuICAgICAqIHBhcmFtZXRlcnMgdG8gYmVjb21lIHJlcXVpcmVkLlxuICAgICAqL1xuICAgIHByaXZhdGUgX3BsYXRmb3JtPztcbiAgICBfYW5pbWF0aW9uTW9kZT86IHN0cmluZyB8IHVuZGVmaW5lZDtcbiAgICBhYnN0cmFjdCBfaXRlbXM6IFF1ZXJ5TGlzdDxNYXRQYWdpbmF0ZWRUYWJIZWFkZXJJdGVtPjtcbiAgICBhYnN0cmFjdCBfaW5rQmFyOiB7XG4gICAgICAgIGhpZGU6ICgpID0+IHZvaWQ7XG4gICAgICAgIGFsaWduVG9FbGVtZW50OiAoZWxlbWVudDogSFRNTEVsZW1lbnQpID0+IHZvaWQ7XG4gICAgfTtcbiAgICBhYnN0cmFjdCBfdGFiTGlzdENvbnRhaW5lcjogRWxlbWVudFJlZjxIVE1MRWxlbWVudD47XG4gICAgYWJzdHJhY3QgX3RhYkxpc3Q6IEVsZW1lbnRSZWY8SFRNTEVsZW1lbnQ+O1xuICAgIGFic3RyYWN0IF9uZXh0UGFnaW5hdG9yOiBFbGVtZW50UmVmPEhUTUxFbGVtZW50PjtcbiAgICBhYnN0cmFjdCBfcHJldmlvdXNQYWdpbmF0b3I6IEVsZW1lbnRSZWY8SFRNTEVsZW1lbnQ+O1xuICAgIC8qKiBUaGUgZGlzdGFuY2UgaW4gcGl4ZWxzIHRoYXQgdGhlIHRhYiBsYWJlbHMgc2hvdWxkIGJlIHRyYW5zbGF0ZWQgdG8gdGhlIGxlZnQuICovXG4gICAgcHJpdmF0ZSBfc2Nyb2xsRGlzdGFuY2U7XG4gICAgLyoqIFdoZXRoZXIgdGhlIGhlYWRlciBzaG91bGQgc2Nyb2xsIHRvIHRoZSBzZWxlY3RlZCBpbmRleCBhZnRlciB0aGUgdmlldyBoYXMgYmVlbiBjaGVja2VkLiAqL1xuICAgIHByaXZhdGUgX3NlbGVjdGVkSW5kZXhDaGFuZ2VkO1xuICAgIC8qKiBFbWl0cyB3aGVuIHRoZSBjb21wb25lbnQgaXMgZGVzdHJveWVkLiAqL1xuICAgIHByb3RlY3RlZCByZWFkb25seSBfZGVzdHJveWVkOiBTdWJqZWN0PHZvaWQ+O1xuICAgIC8qKiBXaGV0aGVyIHRoZSBjb250cm9scyBmb3IgcGFnaW5hdGlvbiBzaG91bGQgYmUgZGlzcGxheWVkICovXG4gICAgX3Nob3dQYWdpbmF0aW9uQ29udHJvbHM6IGJvb2xlYW47XG4gICAgLyoqIFdoZXRoZXIgdGhlIHRhYiBsaXN0IGNhbiBiZSBzY3JvbGxlZCBtb3JlIHRvd2FyZHMgdGhlIGVuZCBvZiB0aGUgdGFiIGxhYmVsIGxpc3QuICovXG4gICAgX2Rpc2FibGVTY3JvbGxBZnRlcjogYm9vbGVhbjtcbiAgICAvKiogV2hldGhlciB0aGUgdGFiIGxpc3QgY2FuIGJlIHNjcm9sbGVkIG1vcmUgdG93YXJkcyB0aGUgYmVnaW5uaW5nIG9mIHRoZSB0YWIgbGFiZWwgbGlzdC4gKi9cbiAgICBfZGlzYWJsZVNjcm9sbEJlZm9yZTogYm9vbGVhbjtcbiAgICAvKipcbiAgICAgKiBUaGUgbnVtYmVyIG9mIHRhYiBsYWJlbHMgdGhhdCBhcmUgZGlzcGxheWVkIG9uIHRoZSBoZWFkZXIuIFdoZW4gdGhpcyBjaGFuZ2VzLCB0aGUgaGVhZGVyXG4gICAgICogc2hvdWxkIHJlLWV2YWx1YXRlIHRoZSBzY3JvbGwgcG9zaXRpb24uXG4gICAgICovXG4gICAgcHJpdmF0ZSBfdGFiTGFiZWxDb3VudDtcbiAgICAvKiogV2hldGhlciB0aGUgc2Nyb2xsIGRpc3RhbmNlIGhhcyBjaGFuZ2VkIGFuZCBzaG91bGQgYmUgYXBwbGllZCBhZnRlciB0aGUgdmlldyBpcyBjaGVja2VkLiAqL1xuICAgIHByaXZhdGUgX3Njcm9sbERpc3RhbmNlQ2hhbmdlZDtcbiAgICAvKiogVXNlZCB0byBtYW5hZ2UgZm9jdXMgYmV0d2VlbiB0aGUgdGFicy4gKi9cbiAgICBwcml2YXRlIF9rZXlNYW5hZ2VyO1xuICAgIC8qKiBDYWNoZWQgdGV4dCBjb250ZW50IG9mIHRoZSBoZWFkZXIuICovXG4gICAgcHJpdmF0ZSBfY3VycmVudFRleHRDb250ZW50O1xuICAgIC8qKiBTdHJlYW0gdGhhdCB3aWxsIHN0b3AgdGhlIGF1dG9tYXRlZCBzY3JvbGxpbmcuICovXG4gICAgcHJpdmF0ZSBfc3RvcFNjcm9sbGluZztcbiAgICAvKipcbiAgICAgKiBXaGV0aGVyIHBhZ2luYXRpb24gc2hvdWxkIGJlIGRpc2FibGVkLiBUaGlzIGNhbiBiZSB1c2VkIHRvIGF2b2lkIHVubmVjZXNzYXJ5XG4gICAgICogbGF5b3V0IHJlY2FsY3VsYXRpb25zIGlmIGl0J3Mga25vd24gdGhhdCBwYWdpbmF0aW9uIHdvbid0IGJlIHJlcXVpcmVkLlxuICAgICAqL1xuICAgIGRpc2FibGVQYWdpbmF0aW9uOiBib29sZWFuO1xuICAgIC8qKiBUaGUgaW5kZXggb2YgdGhlIGFjdGl2ZSB0YWIuICovXG4gICAgZ2V0IHNlbGVjdGVkSW5kZXgoKTogbnVtYmVyO1xuICAgIHNldCBzZWxlY3RlZEluZGV4KHZhbHVlOiBudW1iZXIpO1xuICAgIHByaXZhdGUgX3NlbGVjdGVkSW5kZXg7XG4gICAgLyoqIEV2ZW50IGVtaXR0ZWQgd2hlbiB0aGUgb3B0aW9uIGlzIHNlbGVjdGVkLiAqL1xuICAgIHJlYWRvbmx5IHNlbGVjdEZvY3VzZWRJbmRleDogRXZlbnRFbWl0dGVyPG51bWJlcj47XG4gICAgLyoqIEV2ZW50IGVtaXR0ZWQgd2hlbiBhIGxhYmVsIGlzIGZvY3VzZWQuICovXG4gICAgcmVhZG9ubHkgaW5kZXhGb2N1c2VkOiBFdmVudEVtaXR0ZXI8bnVtYmVyPjtcbiAgICBjb25zdHJ1Y3RvcihfZWxlbWVudFJlZjogRWxlbWVudFJlZjxIVE1MRWxlbWVudD4sIF9jaGFuZ2VEZXRlY3RvclJlZjogQ2hhbmdlRGV0ZWN0b3JSZWYsIF92aWV3cG9ydFJ1bGVyOiBWaWV3cG9ydFJ1bGVyLCBfZGlyOiBEaXJlY3Rpb25hbGl0eSwgX25nWm9uZTogTmdab25lLCBcbiAgICAvKipcbiAgICAgKiBAZGVwcmVjYXRlZCBAYnJlYWtpbmctY2hhbmdlIDkuMC4wIGBfcGxhdGZvcm1gIGFuZCBgX2FuaW1hdGlvbk1vZGVgXG4gICAgICogcGFyYW1ldGVycyB0byBiZWNvbWUgcmVxdWlyZWQuXG4gICAgICovXG4gICAgX3BsYXRmb3JtPzogUGxhdGZvcm0gfCB1bmRlZmluZWQsIF9hbmltYXRpb25Nb2RlPzogc3RyaW5nIHwgdW5kZWZpbmVkKTtcbiAgICAvKiogQ2FsbGVkIHdoZW4gdGhlIHVzZXIgaGFzIHNlbGVjdGVkIGFuIGl0ZW0gdmlhIHRoZSBrZXlib2FyZC4gKi9cbiAgICBwcm90ZWN0ZWQgYWJzdHJhY3QgX2l0ZW1TZWxlY3RlZChldmVudDogS2V5Ym9hcmRFdmVudCk6IHZvaWQ7XG4gICAgbmdBZnRlclZpZXdJbml0KCk6IHZvaWQ7XG4gICAgbmdBZnRlckNvbnRlbnRJbml0KCk6IHZvaWQ7XG4gICAgbmdBZnRlckNvbnRlbnRDaGVja2VkKCk6IHZvaWQ7XG4gICAgbmdPbkRlc3Ryb3koKTogdm9pZDtcbiAgICAvKiogSGFuZGxlcyBrZXlib2FyZCBldmVudHMgb24gdGhlIGhlYWRlci4gKi9cbiAgICBfaGFuZGxlS2V5ZG93bihldmVudDogS2V5Ym9hcmRFdmVudCk6IHZvaWQ7XG4gICAgLyoqXG4gICAgICogQ2FsbGJhY2sgZm9yIHdoZW4gdGhlIE11dGF0aW9uT2JzZXJ2ZXIgZGV0ZWN0cyB0aGF0IHRoZSBjb250ZW50IGhhcyBjaGFuZ2VkLlxuICAgICAqL1xuICAgIF9vbkNvbnRlbnRDaGFuZ2VzKCk6IHZvaWQ7XG4gICAgLyoqXG4gICAgICogVXBkYXRlcyB0aGUgdmlldyB3aGV0aGVyIHBhZ2luYXRpb24gc2hvdWxkIGJlIGVuYWJsZWQgb3Igbm90LlxuICAgICAqXG4gICAgICogV0FSTklORzogQ2FsbGluZyB0aGlzIG1ldGhvZCBjYW4gYmUgdmVyeSBjb3N0bHkgaW4gdGVybXMgb2YgcGVyZm9ybWFuY2UuIEl0IHNob3VsZCBiZSBjYWxsZWRcbiAgICAgKiBhcyBpbmZyZXF1ZW50bHkgYXMgcG9zc2libGUgZnJvbSBvdXRzaWRlIG9mIHRoZSBUYWJzIGNvbXBvbmVudCBhcyBpdCBjYXVzZXMgYSByZWZsb3cgb2YgdGhlXG4gICAgICogcGFnZS5cbiAgICAgKi9cbiAgICB1cGRhdGVQYWdpbmF0aW9uKCk6IHZvaWQ7XG4gICAgLyoqIFRyYWNrcyB3aGljaCBlbGVtZW50IGhhcyBmb2N1czsgdXNlZCBmb3Iga2V5Ym9hcmQgbmF2aWdhdGlvbiAqL1xuICAgIGdldCBmb2N1c0luZGV4KCk6IG51bWJlcjtcbiAgICAvKiogV2hlbiB0aGUgZm9jdXMgaW5kZXggaXMgc2V0LCB3ZSBtdXN0IG1hbnVhbGx5IHNlbmQgZm9jdXMgdG8gdGhlIGNvcnJlY3QgbGFiZWwgKi9cbiAgICBzZXQgZm9jdXNJbmRleCh2YWx1ZTogbnVtYmVyKTtcbiAgICAvKipcbiAgICAgKiBEZXRlcm1pbmVzIGlmIGFuIGluZGV4IGlzIHZhbGlkLiAgSWYgdGhlIHRhYnMgYXJlIG5vdCByZWFkeSB5ZXQsIHdlIGFzc3VtZSB0aGF0IHRoZSB1c2VyIGlzXG4gICAgICogcHJvdmlkaW5nIGEgdmFsaWQgaW5kZXggYW5kIHJldHVybiB0cnVlLlxuICAgICAqL1xuICAgIF9pc1ZhbGlkSW5kZXgoaW5kZXg6IG51bWJlcik6IGJvb2xlYW47XG4gICAgLyoqXG4gICAgICogU2V0cyBmb2N1cyBvbiB0aGUgSFRNTCBlbGVtZW50IGZvciB0aGUgbGFiZWwgd3JhcHBlciBhbmQgc2Nyb2xscyBpdCBpbnRvIHRoZSB2aWV3IGlmXG4gICAgICogc2Nyb2xsaW5nIGlzIGVuYWJsZWQuXG4gICAgICovXG4gICAgX3NldFRhYkZvY3VzKHRhYkluZGV4OiBudW1iZXIpOiB2b2lkO1xuICAgIC8qKiBUaGUgbGF5b3V0IGRpcmVjdGlvbiBvZiB0aGUgY29udGFpbmluZyBhcHAuICovXG4gICAgX2dldExheW91dERpcmVjdGlvbigpOiBEaXJlY3Rpb247XG4gICAgLyoqIFBlcmZvcm1zIHRoZSBDU1MgdHJhbnNmb3JtYXRpb24gb24gdGhlIHRhYiBsaXN0IHRoYXQgd2lsbCBjYXVzZSB0aGUgbGlzdCB0byBzY3JvbGwuICovXG4gICAgX3VwZGF0ZVRhYlNjcm9sbFBvc2l0aW9uKCk6IHZvaWQ7XG4gICAgLyoqIFNldHMgdGhlIGRpc3RhbmNlIGluIHBpeGVscyB0aGF0IHRoZSB0YWIgaGVhZGVyIHNob3VsZCBiZSB0cmFuc2Zvcm1lZCBpbiB0aGUgWC1heGlzLiAqL1xuICAgIGdldCBzY3JvbGxEaXN0YW5jZSgpOiBudW1iZXI7XG4gICAgc2V0IHNjcm9sbERpc3RhbmNlKHZhbHVlOiBudW1iZXIpO1xuICAgIC8qKlxuICAgICAqIE1vdmVzIHRoZSB0YWIgbGlzdCBpbiB0aGUgJ2JlZm9yZScgb3IgJ2FmdGVyJyBkaXJlY3Rpb24gKHRvd2FyZHMgdGhlIGJlZ2lubmluZyBvZiB0aGUgbGlzdCBvclxuICAgICAqIHRoZSBlbmQgb2YgdGhlIGxpc3QsIHJlc3BlY3RpdmVseSkuIFRoZSBkaXN0YW5jZSB0byBzY3JvbGwgaXMgY29tcHV0ZWQgdG8gYmUgYSB0aGlyZCBvZiB0aGVcbiAgICAgKiBsZW5ndGggb2YgdGhlIHRhYiBsaXN0IHZpZXcgd2luZG93LlxuICAgICAqXG4gICAgICogVGhpcyBpcyBhbiBleHBlbnNpdmUgY2FsbCB0aGF0IGZvcmNlcyBhIGxheW91dCByZWZsb3cgdG8gY29tcHV0ZSBib3ggYW5kIHNjcm9sbCBtZXRyaWNzIGFuZFxuICAgICAqIHNob3VsZCBiZSBjYWxsZWQgc3BhcmluZ2x5LlxuICAgICAqL1xuICAgIF9zY3JvbGxIZWFkZXIoZGlyZWN0aW9uOiBTY3JvbGxEaXJlY3Rpb24pOiB7XG4gICAgICAgIG1heFNjcm9sbERpc3RhbmNlOiBudW1iZXI7XG4gICAgICAgIGRpc3RhbmNlOiBudW1iZXI7XG4gICAgfTtcbiAgICAvKiogSGFuZGxlcyBjbGljayBldmVudHMgb24gdGhlIHBhZ2luYXRpb24gYXJyb3dzLiAqL1xuICAgIF9oYW5kbGVQYWdpbmF0b3JDbGljayhkaXJlY3Rpb246IFNjcm9sbERpcmVjdGlvbik6IHZvaWQ7XG4gICAgLyoqXG4gICAgICogTW92ZXMgdGhlIHRhYiBsaXN0IHN1Y2ggdGhhdCB0aGUgZGVzaXJlZCB0YWIgbGFiZWwgKG1hcmtlZCBieSBpbmRleCkgaXMgbW92ZWQgaW50byB2aWV3LlxuICAgICAqXG4gICAgICogVGhpcyBpcyBhbiBleHBlbnNpdmUgY2FsbCB0aGF0IGZvcmNlcyBhIGxheW91dCByZWZsb3cgdG8gY29tcHV0ZSBib3ggYW5kIHNjcm9sbCBtZXRyaWNzIGFuZFxuICAgICAqIHNob3VsZCBiZSBjYWxsZWQgc3BhcmluZ2x5LlxuICAgICAqL1xuICAgIF9zY3JvbGxUb0xhYmVsKGxhYmVsSW5kZXg6IG51bWJlcik6IHZvaWQ7XG4gICAgLyoqXG4gICAgICogRXZhbHVhdGUgd2hldGhlciB0aGUgcGFnaW5hdGlvbiBjb250cm9scyBzaG91bGQgYmUgZGlzcGxheWVkLiBJZiB0aGUgc2Nyb2xsIHdpZHRoIG9mIHRoZVxuICAgICAqIHRhYiBsaXN0IGlzIHdpZGVyIHRoYW4gdGhlIHNpemUgb2YgdGhlIGhlYWRlciBjb250YWluZXIsIHRoZW4gdGhlIHBhZ2luYXRpb24gY29udHJvbHMgc2hvdWxkXG4gICAgICogYmUgc2hvd24uXG4gICAgICpcbiAgICAgKiBUaGlzIGlzIGFuIGV4cGVuc2l2ZSBjYWxsIHRoYXQgZm9yY2VzIGEgbGF5b3V0IHJlZmxvdyB0byBjb21wdXRlIGJveCBhbmQgc2Nyb2xsIG1ldHJpY3MgYW5kXG4gICAgICogc2hvdWxkIGJlIGNhbGxlZCBzcGFyaW5nbHkuXG4gICAgICovXG4gICAgX2NoZWNrUGFnaW5hdGlvbkVuYWJsZWQoKTogdm9pZDtcbiAgICAvKipcbiAgICAgKiBFdmFsdWF0ZSB3aGV0aGVyIHRoZSBiZWZvcmUgYW5kIGFmdGVyIGNvbnRyb2xzIHNob3VsZCBiZSBlbmFibGVkIG9yIGRpc2FibGVkLlxuICAgICAqIElmIHRoZSBoZWFkZXIgaXMgYXQgdGhlIGJlZ2lubmluZyBvZiB0aGUgbGlzdCAoc2Nyb2xsIGRpc3RhbmNlIGlzIGVxdWFsIHRvIDApIHRoZW4gZGlzYWJsZSB0aGVcbiAgICAgKiBiZWZvcmUgYnV0dG9uLiBJZiB0aGUgaGVhZGVyIGlzIGF0IHRoZSBlbmQgb2YgdGhlIGxpc3QgKHNjcm9sbCBkaXN0YW5jZSBpcyBlcXVhbCB0byB0aGVcbiAgICAgKiBtYXhpbXVtIGRpc3RhbmNlIHdlIGNhbiBzY3JvbGwpLCB0aGVuIGRpc2FibGUgdGhlIGFmdGVyIGJ1dHRvbi5cbiAgICAgKlxuICAgICAqIFRoaXMgaXMgYW4gZXhwZW5zaXZlIGNhbGwgdGhhdCBmb3JjZXMgYSBsYXlvdXQgcmVmbG93IHRvIGNvbXB1dGUgYm94IGFuZCBzY3JvbGwgbWV0cmljcyBhbmRcbiAgICAgKiBzaG91bGQgYmUgY2FsbGVkIHNwYXJpbmdseS5cbiAgICAgKi9cbiAgICBfY2hlY2tTY3JvbGxpbmdDb250cm9scygpOiB2b2lkO1xuICAgIC8qKlxuICAgICAqIERldGVybWluZXMgd2hhdCBpcyB0aGUgbWF4aW11bSBsZW5ndGggaW4gcGl4ZWxzIHRoYXQgY2FuIGJlIHNldCBmb3IgdGhlIHNjcm9sbCBkaXN0YW5jZS4gVGhpc1xuICAgICAqIGlzIGVxdWFsIHRvIHRoZSBkaWZmZXJlbmNlIGluIHdpZHRoIGJldHdlZW4gdGhlIHRhYiBsaXN0IGNvbnRhaW5lciBhbmQgdGFiIGhlYWRlciBjb250YWluZXIuXG4gICAgICpcbiAgICAgKiBUaGlzIGlzIGFuIGV4cGVuc2l2ZSBjYWxsIHRoYXQgZm9yY2VzIGEgbGF5b3V0IHJlZmxvdyB0byBjb21wdXRlIGJveCBhbmQgc2Nyb2xsIG1ldHJpY3MgYW5kXG4gICAgICogc2hvdWxkIGJlIGNhbGxlZCBzcGFyaW5nbHkuXG4gICAgICovXG4gICAgX2dldE1heFNjcm9sbERpc3RhbmNlKCk6IG51bWJlcjtcbiAgICAvKiogVGVsbHMgdGhlIGluay1iYXIgdG8gYWxpZ24gaXRzZWxmIHRvIHRoZSBjdXJyZW50IGxhYmVsIHdyYXBwZXIgKi9cbiAgICBfYWxpZ25JbmtCYXJUb1NlbGVjdGVkVGFiKCk6IHZvaWQ7XG4gICAgLyoqIFN0b3BzIHRoZSBjdXJyZW50bHktcnVubmluZyBwYWdpbmF0b3IgaW50ZXJ2YWwuICAqL1xuICAgIF9zdG9wSW50ZXJ2YWwoKTogdm9pZDtcbiAgICAvKipcbiAgICAgKiBIYW5kbGVzIHRoZSB1c2VyIHByZXNzaW5nIGRvd24gb24gb25lIG9mIHRoZSBwYWdpbmF0b3JzLlxuICAgICAqIFN0YXJ0cyBzY3JvbGxpbmcgdGhlIGhlYWRlciBhZnRlciBhIGNlcnRhaW4gYW1vdW50IG9mIHRpbWUuXG4gICAgICogQHBhcmFtIGRpcmVjdGlvbiBJbiB3aGljaCBkaXJlY3Rpb24gdGhlIHBhZ2luYXRvciBzaG91bGQgYmUgc2Nyb2xsZWQuXG4gICAgICovXG4gICAgX2hhbmRsZVBhZ2luYXRvclByZXNzKGRpcmVjdGlvbjogU2Nyb2xsRGlyZWN0aW9uLCBtb3VzZUV2ZW50PzogTW91c2VFdmVudCk6IHZvaWQ7XG4gICAgLyoqXG4gICAgICogU2Nyb2xscyB0aGUgaGVhZGVyIHRvIGEgZ2l2ZW4gcG9zaXRpb24uXG4gICAgICogQHBhcmFtIHBvc2l0aW9uIFBvc2l0aW9uIHRvIHdoaWNoIHRvIHNjcm9sbC5cbiAgICAgKiBAcmV0dXJucyBJbmZvcm1hdGlvbiBvbiB0aGUgY3VycmVudCBzY3JvbGwgZGlzdGFuY2UgYW5kIHRoZSBtYXhpbXVtLlxuICAgICAqL1xuICAgIHByaXZhdGUgX3Njcm9sbFRvO1xuICAgIHN0YXRpYyBuZ0FjY2VwdElucHV0VHlwZV9zZWxlY3RlZEluZGV4OiBOdW1iZXJJbnB1dDtcbn1cbiJdfQ==