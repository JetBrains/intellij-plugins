import { Location } from '@angular/common';
import { ComponentRef, ElementRef, NgZone, OnDestroy, OnInit, EventEmitter, EnvironmentInjector } from '@angular/core';
import type { Provider } from '@angular/core';
import { OutletContext, Router, ActivatedRoute, Data } from '@angular/router';
import type { AnimationBuilder } from '@ionic/core/components';
import { RouteView, StackDidChangeEvent, StackWillChangeEvent } from './stack-utils';
import * as i0 from "@angular/core";
export declare abstract class IonRouterOutlet implements OnDestroy, OnInit {
    readonly parentOutlet?: IonRouterOutlet | undefined;
    abstract outletContent: any;
    nativeEl: HTMLIonRouterOutletElement;
    activatedView: RouteView | null;
    tabsPrefix: string | undefined;
    private _swipeGesture?;
    private stackCtrl;
    private proxyMap;
    private currentActivatedRoute$;
    private activated;
    /** @internal */
    get activatedComponentRef(): ComponentRef<any> | null;
    private _activatedRoute;
    /**
     * The name of the outlet
     */
    name: string;
    /** @internal */
    stackWillChange: EventEmitter<StackWillChangeEvent>;
    /** @internal */
    stackDidChange: EventEmitter<StackDidChangeEvent>;
    activateEvents: EventEmitter<any>;
    deactivateEvents: EventEmitter<any>;
    private parentContexts;
    private location;
    private environmentInjector;
    private inputBinder;
    /** @nodoc */
    readonly supportsBindingToComponentInputs = true;
    private config;
    private navCtrl;
    set animation(animation: AnimationBuilder);
    set animated(animated: boolean);
    set swipeGesture(swipe: boolean);
    constructor(name: string, tabs: string, commonLocation: Location, elementRef: ElementRef, router: Router, zone: NgZone, activatedRoute: ActivatedRoute, parentOutlet?: IonRouterOutlet | undefined);
    ngOnDestroy(): void;
    getContext(): OutletContext | null;
    ngOnInit(): void;
    private initializeOutletWithName;
    get isActivated(): boolean;
    get component(): Record<string, unknown>;
    get activatedRoute(): ActivatedRoute;
    get activatedRouteData(): Data;
    /**
     * Called when the `RouteReuseStrategy` instructs to detach the subtree
     */
    detach(): ComponentRef<any>;
    /**
     * Called when the `RouteReuseStrategy` instructs to re-attach a previously detached subtree
     */
    attach(_ref: ComponentRef<any>, _activatedRoute: ActivatedRoute): void;
    deactivate(): void;
    activateWith(activatedRoute: ActivatedRoute, environmentInjector: EnvironmentInjector | null): void;
    /**
     * Returns `true` if there are pages in the stack to go back.
     */
    canGoBack(deep?: number, stackId?: string): boolean;
    /**
     * Resolves to `true` if it the outlet was able to sucessfully pop the last N pages.
     */
    pop(deep?: number, stackId?: string): Promise<boolean>;
    /**
     * Returns the URL of the active page of each stack.
     */
    getLastUrl(stackId?: string): string | undefined;
    /**
     * Returns the RouteView of the active page of each stack.
     * @internal
     */
    getLastRouteView(stackId?: string): RouteView | undefined;
    /**
     * Returns the root view in the tab stack.
     * @internal
     */
    getRootView(stackId?: string): RouteView | undefined;
    /**
     * Returns the active stack ID. In the context of ion-tabs, it means the active tab.
     */
    getActiveStackId(): string | undefined;
    /**
     * Since the activated route can change over the life time of a component in an ion router outlet, we create
     * a proxy so that we can update the values over time as a user navigates back to components already in the stack.
     */
    private createActivatedRouteProxy;
    /**
     * Create a wrapped observable that will switch to the latest activated route matched by the given component
     */
    private proxyObservable;
    /**
     * Updates the activated route proxy for the given component to the new incoming router state
     */
    private updateActivatedRouteProxy;
    static ɵfac: i0.ɵɵFactoryDeclaration<IonRouterOutlet, [{ attribute: "name"; }, { attribute: "tabs"; optional: true; }, null, null, null, null, null, { optional: true; skipSelf: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<IonRouterOutlet, "ion-router-outlet", ["outlet"], { "animated": { "alias": "animated"; "required": false; }; "animation": { "alias": "animation"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "swipeGesture": { "alias": "swipeGesture"; "required": false; }; "name": { "alias": "name"; "required": false; }; }, { "stackWillChange": "stackWillChange"; "stackDidChange": "stackDidChange"; "activateEvents": "activate"; "deactivateEvents": "deactivate"; }, never, never, false, never>;
}
export declare const provideComponentInputBinding: () => Provider;
