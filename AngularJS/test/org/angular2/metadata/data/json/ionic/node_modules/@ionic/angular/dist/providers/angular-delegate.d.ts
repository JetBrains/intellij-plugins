import { ApplicationRef, ComponentFactoryResolver, Injector, NgZone, ViewContainerRef } from '@angular/core';
import { FrameworkDelegate } from '@ionic/core';
export declare class AngularDelegate {
    private zone;
    private appRef;
    constructor(zone: NgZone, appRef: ApplicationRef);
    create(resolver: ComponentFactoryResolver, injector: Injector, location?: ViewContainerRef): AngularFrameworkDelegate;
}
export declare class AngularFrameworkDelegate implements FrameworkDelegate {
    private resolver;
    private injector;
    private location;
    private appRef;
    private zone;
    private elRefMap;
    private elEventsMap;
    constructor(resolver: ComponentFactoryResolver, injector: Injector, location: ViewContainerRef | undefined, appRef: ApplicationRef, zone: NgZone);
    attachViewToDom(container: any, component: any, params?: any, cssClasses?: string[]): Promise<any>;
    removeViewFromDom(_container: any, component: any): Promise<void>;
}
export declare function attachView(resolver: ComponentFactoryResolver, injector: Injector, location: ViewContainerRef | undefined, appRef: ApplicationRef, elRefMap: WeakMap<HTMLElement, any>, elEventsMap: WeakMap<HTMLElement, () => void>, container: any, component: any, params: any, cssClasses: string[] | undefined): any;
export declare function bindLifecycleEvents(instance: any, element: HTMLElement): () => void;
