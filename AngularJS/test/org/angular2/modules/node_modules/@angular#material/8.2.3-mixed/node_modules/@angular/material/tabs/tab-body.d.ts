/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ChangeDetectorRef, EventEmitter, OnDestroy, OnInit, ElementRef, ComponentFactoryResolver, ViewContainerRef } from '@angular/core';
import { AnimationEvent } from '@angular/animations';
import { TemplatePortal, CdkPortalOutlet, PortalHostDirective } from '@angular/cdk/portal';
import { Directionality, Direction } from '@angular/cdk/bidi';
import { Subject } from 'rxjs';
/**
 * These position states are used internally as animation states for the tab body. Setting the
 * position state to left, right, or center will transition the tab body from its current
 * position to its respective state. If there is not current position (void, in the case of a new
 * tab body), then there will be no transition animation to its state.
 *
 * In the case of a new tab body that should immediately be centered with an animating transition,
 * then left-origin-center or right-origin-center can be used, which will use left or right as its
 * psuedo-prior state.
 */
import * as ɵngcc0 from '@angular/core';
export declare type MatTabBodyPositionState = 'left' | 'center' | 'right' | 'left-origin-center' | 'right-origin-center';
/**
 * The origin state is an internally used state that is set on a new tab body indicating if it
 * began to the left or right of the prior selected index. For example, if the selected index was
 * set to 1, and a new tab is created and selected at index 2, then the tab body would have an
 * origin of right because its index was greater than the prior selected index.
 */
export declare type MatTabBodyOriginState = 'left' | 'right';
/**
 * The portal host directive for the contents of the tab.
 * @docs-private
 */
export declare class MatTabBodyPortal extends CdkPortalOutlet implements OnInit, OnDestroy {
    private _host;
    /** Subscription to events for when the tab body begins centering. */
    private _centeringSub;
    /** Subscription to events for when the tab body finishes leaving from center position. */
    private _leavingSub;
    constructor(componentFactoryResolver: ComponentFactoryResolver, viewContainerRef: ViewContainerRef, _host: MatTabBody, 
    /**
     * @deprecated `_document` parameter to be made required.
     * @breaking-change 9.0.0
     */
    _document?: any);
    /** Set initial visibility or set up subscription for changing visibility. */
    ngOnInit(): void;
    /** Clean up centering subscription. */
    ngOnDestroy(): void;
    static ɵfac: ɵngcc0.ɵɵFactoryDef<MatTabBodyPortal, never>;
    static ɵdir: ɵngcc0.ɵɵDirectiveDefWithMeta<MatTabBodyPortal, "[matTabBodyHost]", never, {}, {}, never>;
}
/**
 * Base class with all of the `MatTabBody` functionality.
 * @docs-private
 */
export declare abstract class _MatTabBodyBase implements OnInit, OnDestroy {
    private _elementRef;
    private _dir;
    /** Current position of the tab-body in the tab-group. Zero means that the tab is visible. */
    private _positionIndex;
    /** Subscription to the directionality change observable. */
    private _dirChangeSubscription;
    /** Tab body position state. Used by the animation trigger for the current state. */
    _position: MatTabBodyPositionState;
    /** Emits when an animation on the tab is complete. */
    _translateTabComplete: Subject<AnimationEvent>;
    /** Event emitted when the tab begins to animate towards the center as the active tab. */
    readonly _onCentering: EventEmitter<number>;
    /** Event emitted before the centering of the tab begins. */
    readonly _beforeCentering: EventEmitter<boolean>;
    /** Event emitted before the centering of the tab begins. */
    readonly _afterLeavingCenter: EventEmitter<void>;
    /** Event emitted when the tab completes its animation towards the center. */
    readonly _onCentered: EventEmitter<void>;
    /** The portal host inside of this container into which the tab body content will be loaded. */
    abstract _portalHost: PortalHostDirective;
    /** The tab body content to display. */
    _content: TemplatePortal;
    /** Position that will be used when the tab is immediately becoming visible after creation. */
    origin: number | null;
    /** Duration for the tab's animation. */
    animationDuration: string;
    /** The shifted index position of the tab body, where zero represents the active center tab. */
    set position(position: number);
    constructor(_elementRef: ElementRef<HTMLElement>, _dir: Directionality, changeDetectorRef: ChangeDetectorRef);
    /**
     * After initialized, check if the content is centered and has an origin. If so, set the
     * special position states that transition the tab from the left or right before centering.
     */
    ngOnInit(): void;
    ngOnDestroy(): void;
    _onTranslateTabStarted(event: AnimationEvent): void;
    /** The text direction of the containing app. */
    _getLayoutDirection(): Direction;
    /** Whether the provided position state is considered center, regardless of origin. */
    _isCenterPosition(position: MatTabBodyPositionState | string): boolean;
    /** Computes the position state that will be used for the tab-body animation trigger. */
    private _computePositionAnimationState;
    /**
     * Computes the position state based on the specified origin position. This is used if the
     * tab is becoming visible immediately after creation.
     */
    private _computePositionFromOrigin;
    static ɵfac: ɵngcc0.ɵɵFactoryDef<_MatTabBodyBase, [null, { optional: true; }, null]>;
    static ɵdir: ɵngcc0.ɵɵDirectiveDefWithMeta<_MatTabBodyBase, never, never, { "animationDuration": "animationDuration"; "position": "position"; "_content": "content"; "origin": "origin"; }, { "_onCentering": "_onCentering"; "_beforeCentering": "_beforeCentering"; "_afterLeavingCenter": "_afterLeavingCenter"; "_onCentered": "_onCentered"; }, never>;
}
/**
 * Wrapper for the contents of a tab.
 * @docs-private
 */
export declare class MatTabBody extends _MatTabBodyBase {
    _portalHost: PortalHostDirective;
    constructor(elementRef: ElementRef<HTMLElement>, dir: Directionality, changeDetectorRef: ChangeDetectorRef);
    static ɵfac: ɵngcc0.ɵɵFactoryDef<MatTabBody, [null, { optional: true; }, null]>;
    static ɵcmp: ɵngcc0.ɵɵComponentDefWithMeta<MatTabBody, "mat-tab-body", never, {}, {}, never, never>;
}

//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoidGFiLWJvZHkuZC50cyIsInNvdXJjZXMiOlsidGFiLWJvZHkuZC50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7O0FBQ0EiLCJzb3VyY2VzQ29udGVudCI6WyIvKipcbiAqIEBsaWNlbnNlXG4gKiBDb3B5cmlnaHQgR29vZ2xlIExMQyBBbGwgUmlnaHRzIFJlc2VydmVkLlxuICpcbiAqIFVzZSBvZiB0aGlzIHNvdXJjZSBjb2RlIGlzIGdvdmVybmVkIGJ5IGFuIE1JVC1zdHlsZSBsaWNlbnNlIHRoYXQgY2FuIGJlXG4gKiBmb3VuZCBpbiB0aGUgTElDRU5TRSBmaWxlIGF0IGh0dHBzOi8vYW5ndWxhci5pby9saWNlbnNlXG4gKi9cbmltcG9ydCB7IENoYW5nZURldGVjdG9yUmVmLCBFdmVudEVtaXR0ZXIsIE9uRGVzdHJveSwgT25Jbml0LCBFbGVtZW50UmVmLCBDb21wb25lbnRGYWN0b3J5UmVzb2x2ZXIsIFZpZXdDb250YWluZXJSZWYgfSBmcm9tICdAYW5ndWxhci9jb3JlJztcbmltcG9ydCB7IEFuaW1hdGlvbkV2ZW50IH0gZnJvbSAnQGFuZ3VsYXIvYW5pbWF0aW9ucyc7XG5pbXBvcnQgeyBUZW1wbGF0ZVBvcnRhbCwgQ2RrUG9ydGFsT3V0bGV0LCBQb3J0YWxIb3N0RGlyZWN0aXZlIH0gZnJvbSAnQGFuZ3VsYXIvY2RrL3BvcnRhbCc7XG5pbXBvcnQgeyBEaXJlY3Rpb25hbGl0eSwgRGlyZWN0aW9uIH0gZnJvbSAnQGFuZ3VsYXIvY2RrL2JpZGknO1xuaW1wb3J0IHsgU3ViamVjdCB9IGZyb20gJ3J4anMnO1xuLyoqXG4gKiBUaGVzZSBwb3NpdGlvbiBzdGF0ZXMgYXJlIHVzZWQgaW50ZXJuYWxseSBhcyBhbmltYXRpb24gc3RhdGVzIGZvciB0aGUgdGFiIGJvZHkuIFNldHRpbmcgdGhlXG4gKiBwb3NpdGlvbiBzdGF0ZSB0byBsZWZ0LCByaWdodCwgb3IgY2VudGVyIHdpbGwgdHJhbnNpdGlvbiB0aGUgdGFiIGJvZHkgZnJvbSBpdHMgY3VycmVudFxuICogcG9zaXRpb24gdG8gaXRzIHJlc3BlY3RpdmUgc3RhdGUuIElmIHRoZXJlIGlzIG5vdCBjdXJyZW50IHBvc2l0aW9uICh2b2lkLCBpbiB0aGUgY2FzZSBvZiBhIG5ld1xuICogdGFiIGJvZHkpLCB0aGVuIHRoZXJlIHdpbGwgYmUgbm8gdHJhbnNpdGlvbiBhbmltYXRpb24gdG8gaXRzIHN0YXRlLlxuICpcbiAqIEluIHRoZSBjYXNlIG9mIGEgbmV3IHRhYiBib2R5IHRoYXQgc2hvdWxkIGltbWVkaWF0ZWx5IGJlIGNlbnRlcmVkIHdpdGggYW4gYW5pbWF0aW5nIHRyYW5zaXRpb24sXG4gKiB0aGVuIGxlZnQtb3JpZ2luLWNlbnRlciBvciByaWdodC1vcmlnaW4tY2VudGVyIGNhbiBiZSB1c2VkLCB3aGljaCB3aWxsIHVzZSBsZWZ0IG9yIHJpZ2h0IGFzIGl0c1xuICogcHN1ZWRvLXByaW9yIHN0YXRlLlxuICovXG5leHBvcnQgZGVjbGFyZSB0eXBlIE1hdFRhYkJvZHlQb3NpdGlvblN0YXRlID0gJ2xlZnQnIHwgJ2NlbnRlcicgfCAncmlnaHQnIHwgJ2xlZnQtb3JpZ2luLWNlbnRlcicgfCAncmlnaHQtb3JpZ2luLWNlbnRlcic7XG4vKipcbiAqIFRoZSBvcmlnaW4gc3RhdGUgaXMgYW4gaW50ZXJuYWxseSB1c2VkIHN0YXRlIHRoYXQgaXMgc2V0IG9uIGEgbmV3IHRhYiBib2R5IGluZGljYXRpbmcgaWYgaXRcbiAqIGJlZ2FuIHRvIHRoZSBsZWZ0IG9yIHJpZ2h0IG9mIHRoZSBwcmlvciBzZWxlY3RlZCBpbmRleC4gRm9yIGV4YW1wbGUsIGlmIHRoZSBzZWxlY3RlZCBpbmRleCB3YXNcbiAqIHNldCB0byAxLCBhbmQgYSBuZXcgdGFiIGlzIGNyZWF0ZWQgYW5kIHNlbGVjdGVkIGF0IGluZGV4IDIsIHRoZW4gdGhlIHRhYiBib2R5IHdvdWxkIGhhdmUgYW5cbiAqIG9yaWdpbiBvZiByaWdodCBiZWNhdXNlIGl0cyBpbmRleCB3YXMgZ3JlYXRlciB0aGFuIHRoZSBwcmlvciBzZWxlY3RlZCBpbmRleC5cbiAqL1xuZXhwb3J0IGRlY2xhcmUgdHlwZSBNYXRUYWJCb2R5T3JpZ2luU3RhdGUgPSAnbGVmdCcgfCAncmlnaHQnO1xuLyoqXG4gKiBUaGUgcG9ydGFsIGhvc3QgZGlyZWN0aXZlIGZvciB0aGUgY29udGVudHMgb2YgdGhlIHRhYi5cbiAqIEBkb2NzLXByaXZhdGVcbiAqL1xuZXhwb3J0IGRlY2xhcmUgY2xhc3MgTWF0VGFiQm9keVBvcnRhbCBleHRlbmRzIENka1BvcnRhbE91dGxldCBpbXBsZW1lbnRzIE9uSW5pdCwgT25EZXN0cm95IHtcbiAgICBwcml2YXRlIF9ob3N0O1xuICAgIC8qKiBTdWJzY3JpcHRpb24gdG8gZXZlbnRzIGZvciB3aGVuIHRoZSB0YWIgYm9keSBiZWdpbnMgY2VudGVyaW5nLiAqL1xuICAgIHByaXZhdGUgX2NlbnRlcmluZ1N1YjtcbiAgICAvKiogU3Vic2NyaXB0aW9uIHRvIGV2ZW50cyBmb3Igd2hlbiB0aGUgdGFiIGJvZHkgZmluaXNoZXMgbGVhdmluZyBmcm9tIGNlbnRlciBwb3NpdGlvbi4gKi9cbiAgICBwcml2YXRlIF9sZWF2aW5nU3ViO1xuICAgIGNvbnN0cnVjdG9yKGNvbXBvbmVudEZhY3RvcnlSZXNvbHZlcjogQ29tcG9uZW50RmFjdG9yeVJlc29sdmVyLCB2aWV3Q29udGFpbmVyUmVmOiBWaWV3Q29udGFpbmVyUmVmLCBfaG9zdDogTWF0VGFiQm9keSwgXG4gICAgLyoqXG4gICAgICogQGRlcHJlY2F0ZWQgYF9kb2N1bWVudGAgcGFyYW1ldGVyIHRvIGJlIG1hZGUgcmVxdWlyZWQuXG4gICAgICogQGJyZWFraW5nLWNoYW5nZSA5LjAuMFxuICAgICAqL1xuICAgIF9kb2N1bWVudD86IGFueSk7XG4gICAgLyoqIFNldCBpbml0aWFsIHZpc2liaWxpdHkgb3Igc2V0IHVwIHN1YnNjcmlwdGlvbiBmb3IgY2hhbmdpbmcgdmlzaWJpbGl0eS4gKi9cbiAgICBuZ09uSW5pdCgpOiB2b2lkO1xuICAgIC8qKiBDbGVhbiB1cCBjZW50ZXJpbmcgc3Vic2NyaXB0aW9uLiAqL1xuICAgIG5nT25EZXN0cm95KCk6IHZvaWQ7XG59XG4vKipcbiAqIEJhc2UgY2xhc3Mgd2l0aCBhbGwgb2YgdGhlIGBNYXRUYWJCb2R5YCBmdW5jdGlvbmFsaXR5LlxuICogQGRvY3MtcHJpdmF0ZVxuICovXG5leHBvcnQgZGVjbGFyZSBhYnN0cmFjdCBjbGFzcyBfTWF0VGFiQm9keUJhc2UgaW1wbGVtZW50cyBPbkluaXQsIE9uRGVzdHJveSB7XG4gICAgcHJpdmF0ZSBfZWxlbWVudFJlZjtcbiAgICBwcml2YXRlIF9kaXI7XG4gICAgLyoqIEN1cnJlbnQgcG9zaXRpb24gb2YgdGhlIHRhYi1ib2R5IGluIHRoZSB0YWItZ3JvdXAuIFplcm8gbWVhbnMgdGhhdCB0aGUgdGFiIGlzIHZpc2libGUuICovXG4gICAgcHJpdmF0ZSBfcG9zaXRpb25JbmRleDtcbiAgICAvKiogU3Vic2NyaXB0aW9uIHRvIHRoZSBkaXJlY3Rpb25hbGl0eSBjaGFuZ2Ugb2JzZXJ2YWJsZS4gKi9cbiAgICBwcml2YXRlIF9kaXJDaGFuZ2VTdWJzY3JpcHRpb247XG4gICAgLyoqIFRhYiBib2R5IHBvc2l0aW9uIHN0YXRlLiBVc2VkIGJ5IHRoZSBhbmltYXRpb24gdHJpZ2dlciBmb3IgdGhlIGN1cnJlbnQgc3RhdGUuICovXG4gICAgX3Bvc2l0aW9uOiBNYXRUYWJCb2R5UG9zaXRpb25TdGF0ZTtcbiAgICAvKiogRW1pdHMgd2hlbiBhbiBhbmltYXRpb24gb24gdGhlIHRhYiBpcyBjb21wbGV0ZS4gKi9cbiAgICBfdHJhbnNsYXRlVGFiQ29tcGxldGU6IFN1YmplY3Q8QW5pbWF0aW9uRXZlbnQ+O1xuICAgIC8qKiBFdmVudCBlbWl0dGVkIHdoZW4gdGhlIHRhYiBiZWdpbnMgdG8gYW5pbWF0ZSB0b3dhcmRzIHRoZSBjZW50ZXIgYXMgdGhlIGFjdGl2ZSB0YWIuICovXG4gICAgcmVhZG9ubHkgX29uQ2VudGVyaW5nOiBFdmVudEVtaXR0ZXI8bnVtYmVyPjtcbiAgICAvKiogRXZlbnQgZW1pdHRlZCBiZWZvcmUgdGhlIGNlbnRlcmluZyBvZiB0aGUgdGFiIGJlZ2lucy4gKi9cbiAgICByZWFkb25seSBfYmVmb3JlQ2VudGVyaW5nOiBFdmVudEVtaXR0ZXI8Ym9vbGVhbj47XG4gICAgLyoqIEV2ZW50IGVtaXR0ZWQgYmVmb3JlIHRoZSBjZW50ZXJpbmcgb2YgdGhlIHRhYiBiZWdpbnMuICovXG4gICAgcmVhZG9ubHkgX2FmdGVyTGVhdmluZ0NlbnRlcjogRXZlbnRFbWl0dGVyPHZvaWQ+O1xuICAgIC8qKiBFdmVudCBlbWl0dGVkIHdoZW4gdGhlIHRhYiBjb21wbGV0ZXMgaXRzIGFuaW1hdGlvbiB0b3dhcmRzIHRoZSBjZW50ZXIuICovXG4gICAgcmVhZG9ubHkgX29uQ2VudGVyZWQ6IEV2ZW50RW1pdHRlcjx2b2lkPjtcbiAgICAvKiogVGhlIHBvcnRhbCBob3N0IGluc2lkZSBvZiB0aGlzIGNvbnRhaW5lciBpbnRvIHdoaWNoIHRoZSB0YWIgYm9keSBjb250ZW50IHdpbGwgYmUgbG9hZGVkLiAqL1xuICAgIGFic3RyYWN0IF9wb3J0YWxIb3N0OiBQb3J0YWxIb3N0RGlyZWN0aXZlO1xuICAgIC8qKiBUaGUgdGFiIGJvZHkgY29udGVudCB0byBkaXNwbGF5LiAqL1xuICAgIF9jb250ZW50OiBUZW1wbGF0ZVBvcnRhbDtcbiAgICAvKiogUG9zaXRpb24gdGhhdCB3aWxsIGJlIHVzZWQgd2hlbiB0aGUgdGFiIGlzIGltbWVkaWF0ZWx5IGJlY29taW5nIHZpc2libGUgYWZ0ZXIgY3JlYXRpb24uICovXG4gICAgb3JpZ2luOiBudW1iZXIgfCBudWxsO1xuICAgIC8qKiBEdXJhdGlvbiBmb3IgdGhlIHRhYidzIGFuaW1hdGlvbi4gKi9cbiAgICBhbmltYXRpb25EdXJhdGlvbjogc3RyaW5nO1xuICAgIC8qKiBUaGUgc2hpZnRlZCBpbmRleCBwb3NpdGlvbiBvZiB0aGUgdGFiIGJvZHksIHdoZXJlIHplcm8gcmVwcmVzZW50cyB0aGUgYWN0aXZlIGNlbnRlciB0YWIuICovXG4gICAgc2V0IHBvc2l0aW9uKHBvc2l0aW9uOiBudW1iZXIpO1xuICAgIGNvbnN0cnVjdG9yKF9lbGVtZW50UmVmOiBFbGVtZW50UmVmPEhUTUxFbGVtZW50PiwgX2RpcjogRGlyZWN0aW9uYWxpdHksIGNoYW5nZURldGVjdG9yUmVmOiBDaGFuZ2VEZXRlY3RvclJlZik7XG4gICAgLyoqXG4gICAgICogQWZ0ZXIgaW5pdGlhbGl6ZWQsIGNoZWNrIGlmIHRoZSBjb250ZW50IGlzIGNlbnRlcmVkIGFuZCBoYXMgYW4gb3JpZ2luLiBJZiBzbywgc2V0IHRoZVxuICAgICAqIHNwZWNpYWwgcG9zaXRpb24gc3RhdGVzIHRoYXQgdHJhbnNpdGlvbiB0aGUgdGFiIGZyb20gdGhlIGxlZnQgb3IgcmlnaHQgYmVmb3JlIGNlbnRlcmluZy5cbiAgICAgKi9cbiAgICBuZ09uSW5pdCgpOiB2b2lkO1xuICAgIG5nT25EZXN0cm95KCk6IHZvaWQ7XG4gICAgX29uVHJhbnNsYXRlVGFiU3RhcnRlZChldmVudDogQW5pbWF0aW9uRXZlbnQpOiB2b2lkO1xuICAgIC8qKiBUaGUgdGV4dCBkaXJlY3Rpb24gb2YgdGhlIGNvbnRhaW5pbmcgYXBwLiAqL1xuICAgIF9nZXRMYXlvdXREaXJlY3Rpb24oKTogRGlyZWN0aW9uO1xuICAgIC8qKiBXaGV0aGVyIHRoZSBwcm92aWRlZCBwb3NpdGlvbiBzdGF0ZSBpcyBjb25zaWRlcmVkIGNlbnRlciwgcmVnYXJkbGVzcyBvZiBvcmlnaW4uICovXG4gICAgX2lzQ2VudGVyUG9zaXRpb24ocG9zaXRpb246IE1hdFRhYkJvZHlQb3NpdGlvblN0YXRlIHwgc3RyaW5nKTogYm9vbGVhbjtcbiAgICAvKiogQ29tcHV0ZXMgdGhlIHBvc2l0aW9uIHN0YXRlIHRoYXQgd2lsbCBiZSB1c2VkIGZvciB0aGUgdGFiLWJvZHkgYW5pbWF0aW9uIHRyaWdnZXIuICovXG4gICAgcHJpdmF0ZSBfY29tcHV0ZVBvc2l0aW9uQW5pbWF0aW9uU3RhdGU7XG4gICAgLyoqXG4gICAgICogQ29tcHV0ZXMgdGhlIHBvc2l0aW9uIHN0YXRlIGJhc2VkIG9uIHRoZSBzcGVjaWZpZWQgb3JpZ2luIHBvc2l0aW9uLiBUaGlzIGlzIHVzZWQgaWYgdGhlXG4gICAgICogdGFiIGlzIGJlY29taW5nIHZpc2libGUgaW1tZWRpYXRlbHkgYWZ0ZXIgY3JlYXRpb24uXG4gICAgICovXG4gICAgcHJpdmF0ZSBfY29tcHV0ZVBvc2l0aW9uRnJvbU9yaWdpbjtcbn1cbi8qKlxuICogV3JhcHBlciBmb3IgdGhlIGNvbnRlbnRzIG9mIGEgdGFiLlxuICogQGRvY3MtcHJpdmF0ZVxuICovXG5leHBvcnQgZGVjbGFyZSBjbGFzcyBNYXRUYWJCb2R5IGV4dGVuZHMgX01hdFRhYkJvZHlCYXNlIHtcbiAgICBfcG9ydGFsSG9zdDogUG9ydGFsSG9zdERpcmVjdGl2ZTtcbiAgICBjb25zdHJ1Y3RvcihlbGVtZW50UmVmOiBFbGVtZW50UmVmPEhUTUxFbGVtZW50PiwgZGlyOiBEaXJlY3Rpb25hbGl0eSwgY2hhbmdlRGV0ZWN0b3JSZWY6IENoYW5nZURldGVjdG9yUmVmKTtcbn1cbiJdfQ==