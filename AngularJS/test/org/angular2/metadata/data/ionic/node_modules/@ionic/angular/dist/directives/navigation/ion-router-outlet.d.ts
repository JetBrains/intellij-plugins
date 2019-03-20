import { ChangeDetectorRef, ComponentFactoryResolver, ComponentRef, ElementRef, EventEmitter, NgZone, OnDestroy, OnInit, ViewContainerRef } from '@angular/core';
import { ActivatedRoute, ChildrenOutletContexts, OutletContext, Router } from '@angular/router';
import { Config } from '../../providers/config';
import { NavController } from '../../providers/nav-controller';
export declare class IonRouterOutlet implements OnDestroy, OnInit {
    private parentContexts;
    private location;
    private resolver;
    private changeDetector;
    private config;
    private navCtrl;
    readonly parentOutlet?: IonRouterOutlet | undefined;
    private activated;
    private activatedView;
    private _activatedRoute;
    private _swipeGesture?;
    private name;
    private stackCtrl;
    private nativeEl;
    tabsPrefix: string | undefined;
    stackEvents: EventEmitter<any>;
    activateEvents: EventEmitter<any>;
    deactivateEvents: EventEmitter<any>;
    animated: boolean;
    swipeGesture: boolean;
    constructor(parentContexts: ChildrenOutletContexts, location: ViewContainerRef, resolver: ComponentFactoryResolver, name: string, tabs: string, changeDetector: ChangeDetectorRef, config: Config, navCtrl: NavController, elementRef: ElementRef, router: Router, zone: NgZone, activatedRoute: ActivatedRoute, parentOutlet?: IonRouterOutlet | undefined);
    ngOnDestroy(): void;
    getContext(): OutletContext | null;
    ngOnInit(): void;
    readonly isActivated: boolean;
    readonly component: object;
    readonly activatedRoute: ActivatedRoute;
    readonly activatedRouteData: any;
    /**
     * Called when the `RouteReuseStrategy` instructs to detach the subtree
     */
    detach(): ComponentRef<any>;
    /**
     * Called when the `RouteReuseStrategy` instructs to re-attach a previously detached subtree
     */
    attach(_ref: ComponentRef<any>, _activatedRoute: ActivatedRoute): void;
    deactivate(): void;
    activateWith(activatedRoute: ActivatedRoute, resolver: ComponentFactoryResolver | null): void;
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
     * Returns the active stack ID. In the context of ion-tabs, it means the active tab.
     */
    getActiveStackId(): string | undefined;
}
