import * as i0 from '@angular/core';
import { ComponentRef, ViewContainerRef, Injector, EmbeddedViewRef, TemplateRef, ElementRef, OnInit, OnDestroy, EventEmitter } from '@angular/core';

/** Interface that can be used to generically type a class. */
interface ComponentType<T> {
    new (...args: any[]): T;
}
/**
 * A `Portal` is something that you want to render somewhere else.
 * It can be attach to / detached from a `PortalOutlet`.
 */
declare abstract class Portal<T> {
    private _attachedHost;
    /** Attach this portal to a host. */
    attach(host: PortalOutlet): T;
    /** Detach this portal from its host */
    detach(): void;
    /** Whether this portal is attached to a host. */
    get isAttached(): boolean;
    /**
     * Sets the PortalOutlet reference without performing `attach()`. This is used directly by
     * the PortalOutlet when it is performing an `attach()` or `detach()`.
     */
    setAttachedHost(host: PortalOutlet | null): void;
}
/**
 * A `ComponentPortal` is a portal that instantiates some Component upon attachment.
 */
declare class ComponentPortal<T> extends Portal<ComponentRef<T>> {
    /** The type of the component that will be instantiated for attachment. */
    component: ComponentType<T>;
    /**
     * Where the attached component should live in Angular's *logical* component tree.
     * This is different from where the component *renders*, which is determined by the PortalOutlet.
     * The origin is necessary when the host is outside of the Angular application context.
     */
    viewContainerRef?: ViewContainerRef | null;
    /** Injector used for the instantiation of the component. */
    injector?: Injector | null;
    /**
     * @deprecated No longer in use. To be removed.
     * @breaking-change 18.0.0
     */
    componentFactoryResolver?: any;
    /**
     * List of DOM nodes that should be projected through `<ng-content>` of the attached component.
     */
    projectableNodes?: Node[][] | null;
    constructor(component: ComponentType<T>, viewContainerRef?: ViewContainerRef | null, injector?: Injector | null, 
    /**
     * @deprecated No longer in use. To be removed.
     * @breaking-change 18.0.0
     */
    _componentFactoryResolver?: any, projectableNodes?: Node[][] | null);
}
/**
 * A `TemplatePortal` is a portal that represents some embedded template (TemplateRef).
 */
declare class TemplatePortal<C = any> extends Portal<EmbeddedViewRef<C>> {
    /** The embedded template that will be used to instantiate an embedded View in the host. */
    templateRef: TemplateRef<C>;
    /** Reference to the ViewContainer into which the template will be stamped out. */
    viewContainerRef: ViewContainerRef;
    /** Contextual data to be passed in to the embedded view. */
    context?: C | undefined;
    /** The injector to use for the embedded view. */
    injector?: Injector | undefined;
    constructor(
    /** The embedded template that will be used to instantiate an embedded View in the host. */
    templateRef: TemplateRef<C>, 
    /** Reference to the ViewContainer into which the template will be stamped out. */
    viewContainerRef: ViewContainerRef, 
    /** Contextual data to be passed in to the embedded view. */
    context?: C | undefined, 
    /** The injector to use for the embedded view. */
    injector?: Injector | undefined);
    get origin(): ElementRef;
    /**
     * Attach the portal to the provided `PortalOutlet`.
     * When a context is provided it will override the `context` property of the `TemplatePortal`
     * instance.
     */
    attach(host: PortalOutlet, context?: C | undefined): EmbeddedViewRef<C>;
    detach(): void;
}
/**
 * A `DomPortal` is a portal whose DOM element will be taken from its current position
 * in the DOM and moved into a portal outlet, when it is attached. On detach, the content
 * will be restored to its original position.
 */
declare class DomPortal<T = HTMLElement> extends Portal<T> {
    /** DOM node hosting the portal's content. */
    readonly element: T;
    constructor(element: T | ElementRef<T>);
}
/** A `PortalOutlet` is a space that can contain a single `Portal`. */
interface PortalOutlet {
    /** Attaches a portal to this outlet. */
    attach(portal: Portal<any>): any;
    /** Detaches the currently attached portal from this outlet. */
    detach(): any;
    /** Performs cleanup before the outlet is destroyed. */
    dispose(): void;
    /** Whether there is currently a portal attached to this outlet. */
    hasAttached(): boolean;
}
/**
 * @deprecated Use `PortalOutlet` instead.
 * @breaking-change 9.0.0
 */
type PortalHost = PortalOutlet;
/**
 * Partial implementation of PortalOutlet that handles attaching
 * ComponentPortal and TemplatePortal.
 */
declare abstract class BasePortalOutlet implements PortalOutlet {
    /** The portal currently attached to the host. */
    protected _attachedPortal: Portal<any> | null;
    /** A function that will permanently dispose this host. */
    private _disposeFn;
    /** Whether this host has already been permanently disposed. */
    private _isDisposed;
    /** Whether this host has an attached portal. */
    hasAttached(): boolean;
    attach<T>(portal: ComponentPortal<T>): ComponentRef<T>;
    attach<T>(portal: TemplatePortal<T>): EmbeddedViewRef<T>;
    attach(portal: any): any;
    abstract attachComponentPortal<T>(portal: ComponentPortal<T>): ComponentRef<T>;
    abstract attachTemplatePortal<C>(portal: TemplatePortal<C>): EmbeddedViewRef<C>;
    readonly attachDomPortal: null | ((portal: DomPortal) => any);
    /** Detaches a previously attached portal. */
    detach(): void;
    /** Permanently dispose of this portal host. */
    dispose(): void;
    /** @docs-private */
    setDisposeFn(fn: () => void): void;
    private _invokeDisposeFn;
}
/**
 * @deprecated Use `BasePortalOutlet` instead.
 * @breaking-change 9.0.0
 */
declare abstract class BasePortalHost extends BasePortalOutlet {
}

/**
 * Directive version of a `TemplatePortal`. Because the directive *is* a TemplatePortal,
 * the directive instance itself can be attached to a host, enabling declarative use of portals.
 */
declare class CdkPortal extends TemplatePortal {
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkPortal, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkPortal, "[cdkPortal]", ["cdkPortal"], {}, {}, never, never, true, never>;
}
/**
 * @deprecated Use `CdkPortal` instead.
 * @breaking-change 9.0.0
 */
declare class TemplatePortalDirective extends CdkPortal {
    static ɵfac: i0.ɵɵFactoryDeclaration<TemplatePortalDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<TemplatePortalDirective, "[cdk-portal], [portal]", ["cdkPortal"], {}, {}, never, never, true, never>;
}
/**
 * Possible attached references to the CdkPortalOutlet.
 */
type CdkPortalOutletAttachedRef = ComponentRef<any> | EmbeddedViewRef<any> | null;
/**
 * Directive version of a PortalOutlet. Because the directive *is* a PortalOutlet, portals can be
 * directly attached to it, enabling declarative use.
 *
 * Usage:
 * `<ng-template [cdkPortalOutlet]="greeting"></ng-template>`
 */
declare class CdkPortalOutlet extends BasePortalOutlet implements OnInit, OnDestroy {
    private _moduleRef;
    private _document;
    private _viewContainerRef;
    /** Whether the portal component is initialized. */
    private _isInitialized;
    /** Reference to the currently-attached component/view ref. */
    private _attachedRef;
    constructor(...args: unknown[]);
    /** Portal associated with the Portal outlet. */
    get portal(): Portal<any> | null;
    set portal(portal: Portal<any> | null | undefined | '');
    /** Emits when a portal is attached to the outlet. */
    readonly attached: EventEmitter<CdkPortalOutletAttachedRef>;
    /** Component or view reference that is attached to the portal. */
    get attachedRef(): CdkPortalOutletAttachedRef;
    ngOnInit(): void;
    ngOnDestroy(): void;
    /**
     * Attach the given ComponentPortal to this PortalOutlet.
     *
     * @param portal Portal to be attached to the portal outlet.
     * @returns Reference to the created component.
     */
    attachComponentPortal<T>(portal: ComponentPortal<T>): ComponentRef<T>;
    /**
     * Attach the given TemplatePortal to this PortalHost as an embedded View.
     * @param portal Portal to be attached.
     * @returns Reference to the created embedded view.
     */
    attachTemplatePortal<C>(portal: TemplatePortal<C>): EmbeddedViewRef<C>;
    /**
     * Attaches the given DomPortal to this PortalHost by moving all of the portal content into it.
     * @param portal Portal to be attached.
     * @deprecated To be turned into a method.
     * @breaking-change 10.0.0
     */
    attachDomPortal: (portal: DomPortal) => void;
    /** Gets the root node of the portal outlet. */
    private _getRootNode;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkPortalOutlet, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkPortalOutlet, "[cdkPortalOutlet]", ["cdkPortalOutlet"], { "portal": { "alias": "cdkPortalOutlet"; "required": false; }; }, { "attached": "attached"; }, never, never, true, never>;
}
/**
 * @deprecated Use `CdkPortalOutlet` instead.
 * @breaking-change 9.0.0
 */
declare class PortalHostDirective extends CdkPortalOutlet {
    static ɵfac: i0.ɵɵFactoryDeclaration<PortalHostDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<PortalHostDirective, "[cdkPortalHost], [portalHost]", ["cdkPortalHost"], { "portal": { "alias": "cdkPortalHost"; "required": false; }; }, {}, never, never, true, never>;
}
declare class PortalModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<PortalModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<PortalModule, never, [typeof CdkPortal, typeof CdkPortalOutlet, typeof TemplatePortalDirective, typeof PortalHostDirective], [typeof CdkPortal, typeof CdkPortalOutlet, typeof TemplatePortalDirective, typeof PortalHostDirective]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<PortalModule>;
}

export { BasePortalHost, BasePortalOutlet, CdkPortal, CdkPortalOutlet, ComponentPortal, DomPortal, Portal, PortalHostDirective, PortalModule, TemplatePortal, TemplatePortalDirective };
export type { CdkPortalOutletAttachedRef, ComponentType, PortalHost, PortalOutlet };
