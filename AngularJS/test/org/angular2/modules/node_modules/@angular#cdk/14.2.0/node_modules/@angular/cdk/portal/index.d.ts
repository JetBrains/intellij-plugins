import { ApplicationRef } from '@angular/core';
import { ComponentFactoryResolver } from '@angular/core';
import { ComponentRef } from '@angular/core';
import { ElementRef } from '@angular/core';
import { EmbeddedViewRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import { Injector } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { TemplateRef } from '@angular/core';
import { ViewContainerRef } from '@angular/core';

/**
 * @deprecated Use `BasePortalOutlet` instead.
 * @breaking-change 9.0.0
 */
export declare abstract class BasePortalHost extends BasePortalOutlet {
}

/**
 * Partial implementation of PortalOutlet that handles attaching
 * ComponentPortal and TemplatePortal.
 */
export declare abstract class BasePortalOutlet implements PortalOutlet {
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
 * Directive version of a `TemplatePortal`. Because the directive *is* a TemplatePortal,
 * the directive instance itself can be attached to a host, enabling declarative use of portals.
 */
export declare class CdkPortal extends TemplatePortal {
    constructor(templateRef: TemplateRef<any>, viewContainerRef: ViewContainerRef);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkPortal, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkPortal, "[cdkPortal]", ["cdkPortal"], {}, {}, never, never, false>;
}

/**
 * Directive version of a PortalOutlet. Because the directive *is* a PortalOutlet, portals can be
 * directly attached to it, enabling declarative use.
 *
 * Usage:
 * `<ng-template [cdkPortalOutlet]="greeting"></ng-template>`
 */
export declare class CdkPortalOutlet extends BasePortalOutlet implements OnInit, OnDestroy {
    private _componentFactoryResolver;
    private _viewContainerRef;
    private _document;
    /** Whether the portal component is initialized. */
    private _isInitialized;
    /** Reference to the currently-attached component/view ref. */
    private _attachedRef;
    constructor(_componentFactoryResolver: ComponentFactoryResolver, _viewContainerRef: ViewContainerRef, 
    /**
     * @deprecated `_document` parameter to be made required.
     * @breaking-change 9.0.0
     */
    _document?: any);
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
     * Attach the given ComponentPortal to this PortalOutlet using the ComponentFactoryResolver.
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
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkPortalOutlet, "[cdkPortalOutlet]", ["cdkPortalOutlet"], { "portal": "cdkPortalOutlet"; }, { "attached": "attached"; }, never, never, false>;
}

/**
 * Possible attached references to the CdkPortalOutlet.
 */
export declare type CdkPortalOutletAttachedRef = ComponentRef<any> | EmbeddedViewRef<any> | null;

/**
 * A `ComponentPortal` is a portal that instantiates some Component upon attachment.
 */
export declare class ComponentPortal<T> extends Portal<ComponentRef<T>> {
    /** The type of the component that will be instantiated for attachment. */
    component: ComponentType<T>;
    /**
     * [Optional] Where the attached component should live in Angular's *logical* component tree.
     * This is different from where the component *renders*, which is determined by the PortalOutlet.
     * The origin is necessary when the host is outside of the Angular application context.
     */
    viewContainerRef?: ViewContainerRef | null;
    /** [Optional] Injector used for the instantiation of the component. */
    injector?: Injector | null;
    /**
     * Alternate `ComponentFactoryResolver` to use when resolving the associated component.
     * Defaults to using the resolver from the outlet that the portal is attached to.
     */
    componentFactoryResolver?: ComponentFactoryResolver | null;
    constructor(component: ComponentType<T>, viewContainerRef?: ViewContainerRef | null, injector?: Injector | null, componentFactoryResolver?: ComponentFactoryResolver | null);
}

/** Interface that can be used to generically type a class. */
export declare interface ComponentType<T> {
    new (...args: any[]): T;
}

/**
 * A `DomPortal` is a portal whose DOM element will be taken from its current position
 * in the DOM and moved into a portal outlet, when it is attached. On detach, the content
 * will be restored to its original position.
 */
export declare class DomPortal<T = HTMLElement> extends Portal<T> {
    /** DOM node hosting the portal's content. */
    readonly element: T;
    constructor(element: T | ElementRef<T>);
}

/**
 * @deprecated Use `DomPortalOutlet` instead.
 * @breaking-change 9.0.0
 */
export declare class DomPortalHost extends DomPortalOutlet {
}

/**
 * A PortalOutlet for attaching portals to an arbitrary DOM element outside of the Angular
 * application context.
 */
export declare class DomPortalOutlet extends BasePortalOutlet {
    /** Element into which the content is projected. */
    outletElement: Element;
    private _componentFactoryResolver?;
    private _appRef?;
    private _defaultInjector?;
    private _document;
    /**
     * @param outletElement Element into which the content is projected.
     * @param _componentFactoryResolver Used to resolve the component factory.
     *   Only required when attaching component portals.
     * @param _appRef Reference to the application. Only used in component portals when there
     *   is no `ViewContainerRef` available.
     * @param _defaultInjector Injector to use as a fallback when the portal being attached doesn't
     *   have one. Only used for component portals.
     * @param _document Reference to the document. Used when attaching a DOM portal. Will eventually
     *   become a required parameter.
     */
    constructor(
    /** Element into which the content is projected. */
    outletElement: Element, _componentFactoryResolver?: ComponentFactoryResolver | undefined, _appRef?: ApplicationRef | undefined, _defaultInjector?: Injector | undefined, 
    /**
     * @deprecated `_document` Parameter to be made required.
     * @breaking-change 10.0.0
     */
    _document?: any);
    /**
     * Attach the given ComponentPortal to DOM element using the ComponentFactoryResolver.
     * @param portal Portal to be attached
     * @returns Reference to the created component.
     */
    attachComponentPortal<T>(portal: ComponentPortal<T>): ComponentRef<T>;
    /**
     * Attaches a template portal to the DOM as an embedded view.
     * @param portal Portal to be attached.
     * @returns Reference to the created embedded view.
     */
    attachTemplatePortal<C>(portal: TemplatePortal<C>): EmbeddedViewRef<C>;
    /**
     * Attaches a DOM portal by transferring its content into the outlet.
     * @param portal Portal to be attached.
     * @deprecated To be turned into a method.
     * @breaking-change 10.0.0
     */
    attachDomPortal: (portal: DomPortal) => void;
    /**
     * Clears out a portal from the DOM.
     */
    dispose(): void;
    /** Gets the root HTMLElement for an instantiated component. */
    private _getComponentRootNode;
}

/**
 * A `Portal` is something that you want to render somewhere else.
 * It can be attach to / detached from a `PortalOutlet`.
 */
export declare abstract class Portal<T> {
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
 * @deprecated Use `PortalOutlet` instead.
 * @breaking-change 9.0.0
 */
export declare type PortalHost = PortalOutlet;

/**
 * @deprecated Use `CdkPortalOutlet` instead.
 * @breaking-change 9.0.0
 */
export declare class PortalHostDirective extends CdkPortalOutlet {
    static ɵfac: i0.ɵɵFactoryDeclaration<PortalHostDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<PortalHostDirective, "[cdkPortalHost], [portalHost]", ["cdkPortalHost"], { "portal": "cdkPortalHost"; }, {}, never, never, false>;
}

/**
 * Custom injector to be used when providing custom
 * injection tokens to components inside a portal.
 * @docs-private
 * @deprecated Use `Injector.create` instead.
 * @breaking-change 11.0.0
 */
export declare class PortalInjector implements Injector {
    private _parentInjector;
    private _customTokens;
    constructor(_parentInjector: Injector, _customTokens: WeakMap<any, any>);
    get(token: any, notFoundValue?: any): any;
}

export declare class PortalModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<PortalModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<PortalModule, [typeof CdkPortal, typeof CdkPortalOutlet, typeof TemplatePortalDirective, typeof PortalHostDirective], never, [typeof CdkPortal, typeof CdkPortalOutlet, typeof TemplatePortalDirective, typeof PortalHostDirective]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<PortalModule>;
}

/** A `PortalOutlet` is an space that can contain a single `Portal`. */
export declare interface PortalOutlet {
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
 * A `TemplatePortal` is a portal that represents some embedded template (TemplateRef).
 */
export declare class TemplatePortal<C = any> extends Portal<EmbeddedViewRef<C>> {
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
 * @deprecated Use `CdkPortal` instead.
 * @breaking-change 9.0.0
 */
export declare class TemplatePortalDirective extends CdkPortal {
    static ɵfac: i0.ɵɵFactoryDeclaration<TemplatePortalDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<TemplatePortalDirective, "[cdk-portal], [portal]", ["cdkPortal"], {}, {}, never, never, false>;
}

export { }
