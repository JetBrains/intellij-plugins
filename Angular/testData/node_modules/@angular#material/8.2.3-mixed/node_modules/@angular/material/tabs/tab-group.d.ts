/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { BooleanInput, NumberInput } from '@angular/cdk/coercion';
import { AfterContentChecked, AfterContentInit, ChangeDetectorRef, ElementRef, EventEmitter, OnDestroy, QueryList } from '@angular/core';
import { CanColor, CanColorCtor, CanDisableRipple, CanDisableRippleCtor, ThemePalette } from '@angular/material/core';
import { MatTab } from './tab';
import { MatTabsConfig } from './tab-config';
/** A simple change event emitted on focus or selection changes. */
import * as ɵngcc0 from '@angular/core';
export declare class MatTabChangeEvent {
    /** Index of the currently-selected tab. */
    index: number;
    /** Reference to the currently-selected tab. */
    tab: MatTab;
}
/** Possible positions for the tab header. */
export declare type MatTabHeaderPosition = 'above' | 'below';
/** @docs-private */
declare class MatTabGroupMixinBase {
    _elementRef: ElementRef;
    constructor(_elementRef: ElementRef);
}
declare const _MatTabGroupMixinBase: CanColorCtor & CanDisableRippleCtor & typeof MatTabGroupMixinBase;
interface MatTabGroupBaseHeader {
    _alignInkBarToSelectedTab: () => void;
    focusIndex: number;
}
/**
 * Base class with all of the `MatTabGroupBase` functionality.
 * @docs-private
 */
export declare abstract class _MatTabGroupBase extends _MatTabGroupMixinBase implements AfterContentInit, AfterContentChecked, OnDestroy, CanColor, CanDisableRipple {
    protected _changeDetectorRef: ChangeDetectorRef;
    _animationMode?: string | undefined;
    /**
     * All tabs inside the tab group. This includes tabs that belong to groups that are nested
     * inside the current one. We filter out only the tabs that belong to this group in `_tabs`.
     */
    abstract _allTabs: QueryList<MatTab>;
    abstract _tabBodyWrapper: ElementRef;
    abstract _tabHeader: MatTabGroupBaseHeader;
    /** All of the tabs that belong to the group. */
    _tabs: QueryList<MatTab>;
    /** The tab index that should be selected after the content has been checked. */
    private _indexToSelect;
    /** Snapshot of the height of the tab body wrapper before another tab is activated. */
    private _tabBodyWrapperHeight;
    /** Subscription to tabs being added/removed. */
    private _tabsSubscription;
    /** Subscription to changes in the tab labels. */
    private _tabLabelSubscription;
    /** Whether the tab group should grow to the size of the active tab. */
    get dynamicHeight(): boolean;
    set dynamicHeight(value: boolean);
    private _dynamicHeight;
    /** The index of the active tab. */
    get selectedIndex(): number | null;
    set selectedIndex(value: number | null);
    private _selectedIndex;
    /** Position of the tab header. */
    headerPosition: MatTabHeaderPosition;
    /** Duration for the tab animation. Will be normalized to milliseconds if no units are set. */
    get animationDuration(): string;
    set animationDuration(value: string);
    private _animationDuration;
    /**
     * Whether pagination should be disabled. This can be used to avoid unnecessary
     * layout recalculations if it's known that pagination won't be required.
     */
    disablePagination: boolean;
    /** Background color of the tab group. */
    get backgroundColor(): ThemePalette;
    set backgroundColor(value: ThemePalette);
    private _backgroundColor;
    /** Output to enable support for two-way binding on `[(selectedIndex)]` */
    readonly selectedIndexChange: EventEmitter<number>;
    /** Event emitted when focus has changed within a tab group. */
    readonly focusChange: EventEmitter<MatTabChangeEvent>;
    /** Event emitted when the body animation has completed */
    readonly animationDone: EventEmitter<void>;
    /** Event emitted when the tab selection has changed. */
    readonly selectedTabChange: EventEmitter<MatTabChangeEvent>;
    private _groupId;
    constructor(elementRef: ElementRef, _changeDetectorRef: ChangeDetectorRef, defaultConfig?: MatTabsConfig, _animationMode?: string | undefined);
    /**
     * After the content is checked, this component knows what tabs have been defined
     * and what the selected index should be. This is where we can know exactly what position
     * each tab should be in according to the new selected index, and additionally we know how
     * a new selected tab should transition in (from the left or right).
     */
    ngAfterContentChecked(): void;
    ngAfterContentInit(): void;
    /** Listens to changes in all of the tabs. */
    private _subscribeToAllTabChanges;
    ngOnDestroy(): void;
    /** Re-aligns the ink bar to the selected tab element. */
    realignInkBar(): void;
    _focusChanged(index: number): void;
    private _createChangeEvent;
    /**
     * Subscribes to changes in the tab labels. This is needed, because the @Input for the label is
     * on the MatTab component, whereas the data binding is inside the MatTabGroup. In order for the
     * binding to be updated, we need to subscribe to changes in it and trigger change detection
     * manually.
     */
    private _subscribeToTabLabels;
    /** Clamps the given index to the bounds of 0 and the tabs length. */
    private _clampTabIndex;
    /** Returns a unique id for each tab label element */
    _getTabLabelId(i: number): string;
    /** Returns a unique id for each tab content element */
    _getTabContentId(i: number): string;
    /**
     * Sets the height of the body wrapper to the height of the activating tab if dynamic
     * height property is true.
     */
    _setTabBodyWrapperHeight(tabHeight: number): void;
    /** Removes the height of the tab body wrapper. */
    _removeTabBodyWrapperHeight(): void;
    /** Handle click events, setting new selected index if appropriate. */
    _handleClick(tab: MatTab, tabHeader: MatTabGroupBaseHeader, index: number): void;
    /** Retrieves the tabindex for the tab. */
    _getTabIndex(tab: MatTab, idx: number): number | null;
    static ngAcceptInputType_dynamicHeight: BooleanInput;
    static ngAcceptInputType_animationDuration: NumberInput;
    static ngAcceptInputType_selectedIndex: NumberInput;
    static ngAcceptInputType_disableRipple: BooleanInput;
    static ɵfac: ɵngcc0.ɵɵFactoryDef<_MatTabGroupBase, [null, null, { optional: true; }, { optional: true; }]>;
    static ɵdir: ɵngcc0.ɵɵDirectiveDefWithMeta<_MatTabGroupBase, never, never, { "headerPosition": "headerPosition"; "animationDuration": "animationDuration"; "disablePagination": "disablePagination"; "dynamicHeight": "dynamicHeight"; "selectedIndex": "selectedIndex"; "backgroundColor": "backgroundColor"; }, { "selectedIndexChange": "selectedIndexChange"; "focusChange": "focusChange"; "animationDone": "animationDone"; "selectedTabChange": "selectedTabChange"; }, never>;
}
/**
 * Material design tab-group component. Supports basic tab pairs (label + content) and includes
 * animated ink-bar, keyboard navigation, and screen reader.
 * See: https://material.io/design/components/tabs.html
 */
export declare class MatTabGroup extends _MatTabGroupBase {
    _allTabs: QueryList<MatTab>;
    _tabBodyWrapper: ElementRef;
    _tabHeader: MatTabGroupBaseHeader;
    constructor(elementRef: ElementRef, changeDetectorRef: ChangeDetectorRef, defaultConfig?: MatTabsConfig, animationMode?: string);
    static ɵfac: ɵngcc0.ɵɵFactoryDef<MatTabGroup, [null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: ɵngcc0.ɵɵComponentDefWithMeta<MatTabGroup, "mat-tab-group", ["matTabGroup"], { "color": "color"; "disableRipple": "disableRipple"; }, {}, ["_allTabs"], never>;
}
export {};

//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoidGFiLWdyb3VwLmQudHMiLCJzb3VyY2VzIjpbInRhYi1ncm91cC5kLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7QUFDQTtBQUNBIiwic291cmNlc0NvbnRlbnQiOlsiLyoqXG4gKiBAbGljZW5zZVxuICogQ29weXJpZ2h0IEdvb2dsZSBMTEMgQWxsIFJpZ2h0cyBSZXNlcnZlZC5cbiAqXG4gKiBVc2Ugb2YgdGhpcyBzb3VyY2UgY29kZSBpcyBnb3Zlcm5lZCBieSBhbiBNSVQtc3R5bGUgbGljZW5zZSB0aGF0IGNhbiBiZVxuICogZm91bmQgaW4gdGhlIExJQ0VOU0UgZmlsZSBhdCBodHRwczovL2FuZ3VsYXIuaW8vbGljZW5zZVxuICovXG5pbXBvcnQgeyBCb29sZWFuSW5wdXQsIE51bWJlcklucHV0IH0gZnJvbSAnQGFuZ3VsYXIvY2RrL2NvZXJjaW9uJztcbmltcG9ydCB7IEFmdGVyQ29udGVudENoZWNrZWQsIEFmdGVyQ29udGVudEluaXQsIENoYW5nZURldGVjdG9yUmVmLCBFbGVtZW50UmVmLCBFdmVudEVtaXR0ZXIsIE9uRGVzdHJveSwgUXVlcnlMaXN0IH0gZnJvbSAnQGFuZ3VsYXIvY29yZSc7XG5pbXBvcnQgeyBDYW5Db2xvciwgQ2FuQ29sb3JDdG9yLCBDYW5EaXNhYmxlUmlwcGxlLCBDYW5EaXNhYmxlUmlwcGxlQ3RvciwgVGhlbWVQYWxldHRlIH0gZnJvbSAnQGFuZ3VsYXIvbWF0ZXJpYWwvY29yZSc7XG5pbXBvcnQgeyBNYXRUYWIgfSBmcm9tICcuL3RhYic7XG5pbXBvcnQgeyBNYXRUYWJzQ29uZmlnIH0gZnJvbSAnLi90YWItY29uZmlnJztcbi8qKiBBIHNpbXBsZSBjaGFuZ2UgZXZlbnQgZW1pdHRlZCBvbiBmb2N1cyBvciBzZWxlY3Rpb24gY2hhbmdlcy4gKi9cbmV4cG9ydCBkZWNsYXJlIGNsYXNzIE1hdFRhYkNoYW5nZUV2ZW50IHtcbiAgICAvKiogSW5kZXggb2YgdGhlIGN1cnJlbnRseS1zZWxlY3RlZCB0YWIuICovXG4gICAgaW5kZXg6IG51bWJlcjtcbiAgICAvKiogUmVmZXJlbmNlIHRvIHRoZSBjdXJyZW50bHktc2VsZWN0ZWQgdGFiLiAqL1xuICAgIHRhYjogTWF0VGFiO1xufVxuLyoqIFBvc3NpYmxlIHBvc2l0aW9ucyBmb3IgdGhlIHRhYiBoZWFkZXIuICovXG5leHBvcnQgZGVjbGFyZSB0eXBlIE1hdFRhYkhlYWRlclBvc2l0aW9uID0gJ2Fib3ZlJyB8ICdiZWxvdyc7XG4vKiogQGRvY3MtcHJpdmF0ZSAqL1xuZGVjbGFyZSBjbGFzcyBNYXRUYWJHcm91cE1peGluQmFzZSB7XG4gICAgX2VsZW1lbnRSZWY6IEVsZW1lbnRSZWY7XG4gICAgY29uc3RydWN0b3IoX2VsZW1lbnRSZWY6IEVsZW1lbnRSZWYpO1xufVxuZGVjbGFyZSBjb25zdCBfTWF0VGFiR3JvdXBNaXhpbkJhc2U6IENhbkNvbG9yQ3RvciAmIENhbkRpc2FibGVSaXBwbGVDdG9yICYgdHlwZW9mIE1hdFRhYkdyb3VwTWl4aW5CYXNlO1xuaW50ZXJmYWNlIE1hdFRhYkdyb3VwQmFzZUhlYWRlciB7XG4gICAgX2FsaWduSW5rQmFyVG9TZWxlY3RlZFRhYjogKCkgPT4gdm9pZDtcbiAgICBmb2N1c0luZGV4OiBudW1iZXI7XG59XG4vKipcbiAqIEJhc2UgY2xhc3Mgd2l0aCBhbGwgb2YgdGhlIGBNYXRUYWJHcm91cEJhc2VgIGZ1bmN0aW9uYWxpdHkuXG4gKiBAZG9jcy1wcml2YXRlXG4gKi9cbmV4cG9ydCBkZWNsYXJlIGFic3RyYWN0IGNsYXNzIF9NYXRUYWJHcm91cEJhc2UgZXh0ZW5kcyBfTWF0VGFiR3JvdXBNaXhpbkJhc2UgaW1wbGVtZW50cyBBZnRlckNvbnRlbnRJbml0LCBBZnRlckNvbnRlbnRDaGVja2VkLCBPbkRlc3Ryb3ksIENhbkNvbG9yLCBDYW5EaXNhYmxlUmlwcGxlIHtcbiAgICBwcm90ZWN0ZWQgX2NoYW5nZURldGVjdG9yUmVmOiBDaGFuZ2VEZXRlY3RvclJlZjtcbiAgICBfYW5pbWF0aW9uTW9kZT86IHN0cmluZyB8IHVuZGVmaW5lZDtcbiAgICAvKipcbiAgICAgKiBBbGwgdGFicyBpbnNpZGUgdGhlIHRhYiBncm91cC4gVGhpcyBpbmNsdWRlcyB0YWJzIHRoYXQgYmVsb25nIHRvIGdyb3VwcyB0aGF0IGFyZSBuZXN0ZWRcbiAgICAgKiBpbnNpZGUgdGhlIGN1cnJlbnQgb25lLiBXZSBmaWx0ZXIgb3V0IG9ubHkgdGhlIHRhYnMgdGhhdCBiZWxvbmcgdG8gdGhpcyBncm91cCBpbiBgX3RhYnNgLlxuICAgICAqL1xuICAgIGFic3RyYWN0IF9hbGxUYWJzOiBRdWVyeUxpc3Q8TWF0VGFiPjtcbiAgICBhYnN0cmFjdCBfdGFiQm9keVdyYXBwZXI6IEVsZW1lbnRSZWY7XG4gICAgYWJzdHJhY3QgX3RhYkhlYWRlcjogTWF0VGFiR3JvdXBCYXNlSGVhZGVyO1xuICAgIC8qKiBBbGwgb2YgdGhlIHRhYnMgdGhhdCBiZWxvbmcgdG8gdGhlIGdyb3VwLiAqL1xuICAgIF90YWJzOiBRdWVyeUxpc3Q8TWF0VGFiPjtcbiAgICAvKiogVGhlIHRhYiBpbmRleCB0aGF0IHNob3VsZCBiZSBzZWxlY3RlZCBhZnRlciB0aGUgY29udGVudCBoYXMgYmVlbiBjaGVja2VkLiAqL1xuICAgIHByaXZhdGUgX2luZGV4VG9TZWxlY3Q7XG4gICAgLyoqIFNuYXBzaG90IG9mIHRoZSBoZWlnaHQgb2YgdGhlIHRhYiBib2R5IHdyYXBwZXIgYmVmb3JlIGFub3RoZXIgdGFiIGlzIGFjdGl2YXRlZC4gKi9cbiAgICBwcml2YXRlIF90YWJCb2R5V3JhcHBlckhlaWdodDtcbiAgICAvKiogU3Vic2NyaXB0aW9uIHRvIHRhYnMgYmVpbmcgYWRkZWQvcmVtb3ZlZC4gKi9cbiAgICBwcml2YXRlIF90YWJzU3Vic2NyaXB0aW9uO1xuICAgIC8qKiBTdWJzY3JpcHRpb24gdG8gY2hhbmdlcyBpbiB0aGUgdGFiIGxhYmVscy4gKi9cbiAgICBwcml2YXRlIF90YWJMYWJlbFN1YnNjcmlwdGlvbjtcbiAgICAvKiogV2hldGhlciB0aGUgdGFiIGdyb3VwIHNob3VsZCBncm93IHRvIHRoZSBzaXplIG9mIHRoZSBhY3RpdmUgdGFiLiAqL1xuICAgIGdldCBkeW5hbWljSGVpZ2h0KCk6IGJvb2xlYW47XG4gICAgc2V0IGR5bmFtaWNIZWlnaHQodmFsdWU6IGJvb2xlYW4pO1xuICAgIHByaXZhdGUgX2R5bmFtaWNIZWlnaHQ7XG4gICAgLyoqIFRoZSBpbmRleCBvZiB0aGUgYWN0aXZlIHRhYi4gKi9cbiAgICBnZXQgc2VsZWN0ZWRJbmRleCgpOiBudW1iZXIgfCBudWxsO1xuICAgIHNldCBzZWxlY3RlZEluZGV4KHZhbHVlOiBudW1iZXIgfCBudWxsKTtcbiAgICBwcml2YXRlIF9zZWxlY3RlZEluZGV4O1xuICAgIC8qKiBQb3NpdGlvbiBvZiB0aGUgdGFiIGhlYWRlci4gKi9cbiAgICBoZWFkZXJQb3NpdGlvbjogTWF0VGFiSGVhZGVyUG9zaXRpb247XG4gICAgLyoqIER1cmF0aW9uIGZvciB0aGUgdGFiIGFuaW1hdGlvbi4gV2lsbCBiZSBub3JtYWxpemVkIHRvIG1pbGxpc2Vjb25kcyBpZiBubyB1bml0cyBhcmUgc2V0LiAqL1xuICAgIGdldCBhbmltYXRpb25EdXJhdGlvbigpOiBzdHJpbmc7XG4gICAgc2V0IGFuaW1hdGlvbkR1cmF0aW9uKHZhbHVlOiBzdHJpbmcpO1xuICAgIHByaXZhdGUgX2FuaW1hdGlvbkR1cmF0aW9uO1xuICAgIC8qKlxuICAgICAqIFdoZXRoZXIgcGFnaW5hdGlvbiBzaG91bGQgYmUgZGlzYWJsZWQuIFRoaXMgY2FuIGJlIHVzZWQgdG8gYXZvaWQgdW5uZWNlc3NhcnlcbiAgICAgKiBsYXlvdXQgcmVjYWxjdWxhdGlvbnMgaWYgaXQncyBrbm93biB0aGF0IHBhZ2luYXRpb24gd29uJ3QgYmUgcmVxdWlyZWQuXG4gICAgICovXG4gICAgZGlzYWJsZVBhZ2luYXRpb246IGJvb2xlYW47XG4gICAgLyoqIEJhY2tncm91bmQgY29sb3Igb2YgdGhlIHRhYiBncm91cC4gKi9cbiAgICBnZXQgYmFja2dyb3VuZENvbG9yKCk6IFRoZW1lUGFsZXR0ZTtcbiAgICBzZXQgYmFja2dyb3VuZENvbG9yKHZhbHVlOiBUaGVtZVBhbGV0dGUpO1xuICAgIHByaXZhdGUgX2JhY2tncm91bmRDb2xvcjtcbiAgICAvKiogT3V0cHV0IHRvIGVuYWJsZSBzdXBwb3J0IGZvciB0d28td2F5IGJpbmRpbmcgb24gYFsoc2VsZWN0ZWRJbmRleCldYCAqL1xuICAgIHJlYWRvbmx5IHNlbGVjdGVkSW5kZXhDaGFuZ2U6IEV2ZW50RW1pdHRlcjxudW1iZXI+O1xuICAgIC8qKiBFdmVudCBlbWl0dGVkIHdoZW4gZm9jdXMgaGFzIGNoYW5nZWQgd2l0aGluIGEgdGFiIGdyb3VwLiAqL1xuICAgIHJlYWRvbmx5IGZvY3VzQ2hhbmdlOiBFdmVudEVtaXR0ZXI8TWF0VGFiQ2hhbmdlRXZlbnQ+O1xuICAgIC8qKiBFdmVudCBlbWl0dGVkIHdoZW4gdGhlIGJvZHkgYW5pbWF0aW9uIGhhcyBjb21wbGV0ZWQgKi9cbiAgICByZWFkb25seSBhbmltYXRpb25Eb25lOiBFdmVudEVtaXR0ZXI8dm9pZD47XG4gICAgLyoqIEV2ZW50IGVtaXR0ZWQgd2hlbiB0aGUgdGFiIHNlbGVjdGlvbiBoYXMgY2hhbmdlZC4gKi9cbiAgICByZWFkb25seSBzZWxlY3RlZFRhYkNoYW5nZTogRXZlbnRFbWl0dGVyPE1hdFRhYkNoYW5nZUV2ZW50PjtcbiAgICBwcml2YXRlIF9ncm91cElkO1xuICAgIGNvbnN0cnVjdG9yKGVsZW1lbnRSZWY6IEVsZW1lbnRSZWYsIF9jaGFuZ2VEZXRlY3RvclJlZjogQ2hhbmdlRGV0ZWN0b3JSZWYsIGRlZmF1bHRDb25maWc/OiBNYXRUYWJzQ29uZmlnLCBfYW5pbWF0aW9uTW9kZT86IHN0cmluZyB8IHVuZGVmaW5lZCk7XG4gICAgLyoqXG4gICAgICogQWZ0ZXIgdGhlIGNvbnRlbnQgaXMgY2hlY2tlZCwgdGhpcyBjb21wb25lbnQga25vd3Mgd2hhdCB0YWJzIGhhdmUgYmVlbiBkZWZpbmVkXG4gICAgICogYW5kIHdoYXQgdGhlIHNlbGVjdGVkIGluZGV4IHNob3VsZCBiZS4gVGhpcyBpcyB3aGVyZSB3ZSBjYW4ga25vdyBleGFjdGx5IHdoYXQgcG9zaXRpb25cbiAgICAgKiBlYWNoIHRhYiBzaG91bGQgYmUgaW4gYWNjb3JkaW5nIHRvIHRoZSBuZXcgc2VsZWN0ZWQgaW5kZXgsIGFuZCBhZGRpdGlvbmFsbHkgd2Uga25vdyBob3dcbiAgICAgKiBhIG5ldyBzZWxlY3RlZCB0YWIgc2hvdWxkIHRyYW5zaXRpb24gaW4gKGZyb20gdGhlIGxlZnQgb3IgcmlnaHQpLlxuICAgICAqL1xuICAgIG5nQWZ0ZXJDb250ZW50Q2hlY2tlZCgpOiB2b2lkO1xuICAgIG5nQWZ0ZXJDb250ZW50SW5pdCgpOiB2b2lkO1xuICAgIC8qKiBMaXN0ZW5zIHRvIGNoYW5nZXMgaW4gYWxsIG9mIHRoZSB0YWJzLiAqL1xuICAgIHByaXZhdGUgX3N1YnNjcmliZVRvQWxsVGFiQ2hhbmdlcztcbiAgICBuZ09uRGVzdHJveSgpOiB2b2lkO1xuICAgIC8qKiBSZS1hbGlnbnMgdGhlIGluayBiYXIgdG8gdGhlIHNlbGVjdGVkIHRhYiBlbGVtZW50LiAqL1xuICAgIHJlYWxpZ25JbmtCYXIoKTogdm9pZDtcbiAgICBfZm9jdXNDaGFuZ2VkKGluZGV4OiBudW1iZXIpOiB2b2lkO1xuICAgIHByaXZhdGUgX2NyZWF0ZUNoYW5nZUV2ZW50O1xuICAgIC8qKlxuICAgICAqIFN1YnNjcmliZXMgdG8gY2hhbmdlcyBpbiB0aGUgdGFiIGxhYmVscy4gVGhpcyBpcyBuZWVkZWQsIGJlY2F1c2UgdGhlIEBJbnB1dCBmb3IgdGhlIGxhYmVsIGlzXG4gICAgICogb24gdGhlIE1hdFRhYiBjb21wb25lbnQsIHdoZXJlYXMgdGhlIGRhdGEgYmluZGluZyBpcyBpbnNpZGUgdGhlIE1hdFRhYkdyb3VwLiBJbiBvcmRlciBmb3IgdGhlXG4gICAgICogYmluZGluZyB0byBiZSB1cGRhdGVkLCB3ZSBuZWVkIHRvIHN1YnNjcmliZSB0byBjaGFuZ2VzIGluIGl0IGFuZCB0cmlnZ2VyIGNoYW5nZSBkZXRlY3Rpb25cbiAgICAgKiBtYW51YWxseS5cbiAgICAgKi9cbiAgICBwcml2YXRlIF9zdWJzY3JpYmVUb1RhYkxhYmVscztcbiAgICAvKiogQ2xhbXBzIHRoZSBnaXZlbiBpbmRleCB0byB0aGUgYm91bmRzIG9mIDAgYW5kIHRoZSB0YWJzIGxlbmd0aC4gKi9cbiAgICBwcml2YXRlIF9jbGFtcFRhYkluZGV4O1xuICAgIC8qKiBSZXR1cm5zIGEgdW5pcXVlIGlkIGZvciBlYWNoIHRhYiBsYWJlbCBlbGVtZW50ICovXG4gICAgX2dldFRhYkxhYmVsSWQoaTogbnVtYmVyKTogc3RyaW5nO1xuICAgIC8qKiBSZXR1cm5zIGEgdW5pcXVlIGlkIGZvciBlYWNoIHRhYiBjb250ZW50IGVsZW1lbnQgKi9cbiAgICBfZ2V0VGFiQ29udGVudElkKGk6IG51bWJlcik6IHN0cmluZztcbiAgICAvKipcbiAgICAgKiBTZXRzIHRoZSBoZWlnaHQgb2YgdGhlIGJvZHkgd3JhcHBlciB0byB0aGUgaGVpZ2h0IG9mIHRoZSBhY3RpdmF0aW5nIHRhYiBpZiBkeW5hbWljXG4gICAgICogaGVpZ2h0IHByb3BlcnR5IGlzIHRydWUuXG4gICAgICovXG4gICAgX3NldFRhYkJvZHlXcmFwcGVySGVpZ2h0KHRhYkhlaWdodDogbnVtYmVyKTogdm9pZDtcbiAgICAvKiogUmVtb3ZlcyB0aGUgaGVpZ2h0IG9mIHRoZSB0YWIgYm9keSB3cmFwcGVyLiAqL1xuICAgIF9yZW1vdmVUYWJCb2R5V3JhcHBlckhlaWdodCgpOiB2b2lkO1xuICAgIC8qKiBIYW5kbGUgY2xpY2sgZXZlbnRzLCBzZXR0aW5nIG5ldyBzZWxlY3RlZCBpbmRleCBpZiBhcHByb3ByaWF0ZS4gKi9cbiAgICBfaGFuZGxlQ2xpY2sodGFiOiBNYXRUYWIsIHRhYkhlYWRlcjogTWF0VGFiR3JvdXBCYXNlSGVhZGVyLCBpbmRleDogbnVtYmVyKTogdm9pZDtcbiAgICAvKiogUmV0cmlldmVzIHRoZSB0YWJpbmRleCBmb3IgdGhlIHRhYi4gKi9cbiAgICBfZ2V0VGFiSW5kZXgodGFiOiBNYXRUYWIsIGlkeDogbnVtYmVyKTogbnVtYmVyIHwgbnVsbDtcbiAgICBzdGF0aWMgbmdBY2NlcHRJbnB1dFR5cGVfZHluYW1pY0hlaWdodDogQm9vbGVhbklucHV0O1xuICAgIHN0YXRpYyBuZ0FjY2VwdElucHV0VHlwZV9hbmltYXRpb25EdXJhdGlvbjogTnVtYmVySW5wdXQ7XG4gICAgc3RhdGljIG5nQWNjZXB0SW5wdXRUeXBlX3NlbGVjdGVkSW5kZXg6IE51bWJlcklucHV0O1xuICAgIHN0YXRpYyBuZ0FjY2VwdElucHV0VHlwZV9kaXNhYmxlUmlwcGxlOiBCb29sZWFuSW5wdXQ7XG59XG4vKipcbiAqIE1hdGVyaWFsIGRlc2lnbiB0YWItZ3JvdXAgY29tcG9uZW50LiBTdXBwb3J0cyBiYXNpYyB0YWIgcGFpcnMgKGxhYmVsICsgY29udGVudCkgYW5kIGluY2x1ZGVzXG4gKiBhbmltYXRlZCBpbmstYmFyLCBrZXlib2FyZCBuYXZpZ2F0aW9uLCBhbmQgc2NyZWVuIHJlYWRlci5cbiAqIFNlZTogaHR0cHM6Ly9tYXRlcmlhbC5pby9kZXNpZ24vY29tcG9uZW50cy90YWJzLmh0bWxcbiAqL1xuZXhwb3J0IGRlY2xhcmUgY2xhc3MgTWF0VGFiR3JvdXAgZXh0ZW5kcyBfTWF0VGFiR3JvdXBCYXNlIHtcbiAgICBfYWxsVGFiczogUXVlcnlMaXN0PE1hdFRhYj47XG4gICAgX3RhYkJvZHlXcmFwcGVyOiBFbGVtZW50UmVmO1xuICAgIF90YWJIZWFkZXI6IE1hdFRhYkdyb3VwQmFzZUhlYWRlcjtcbiAgICBjb25zdHJ1Y3RvcihlbGVtZW50UmVmOiBFbGVtZW50UmVmLCBjaGFuZ2VEZXRlY3RvclJlZjogQ2hhbmdlRGV0ZWN0b3JSZWYsIGRlZmF1bHRDb25maWc/OiBNYXRUYWJzQ29uZmlnLCBhbmltYXRpb25Nb2RlPzogc3RyaW5nKTtcbn1cbmV4cG9ydCB7fTtcbiJdfQ==