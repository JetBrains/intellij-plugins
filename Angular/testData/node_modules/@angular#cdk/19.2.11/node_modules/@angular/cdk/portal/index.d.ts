import { BasePortalOutlet, ComponentPortal, TemplatePortal, DomPortal } from '../portal-directives.d-C698lRc2.js';
export { BasePortalHost, CdkPortal, CdkPortalOutlet, CdkPortalOutletAttachedRef, ComponentType, Portal, PortalHost, PortalHostDirective, PortalModule, PortalOutlet, TemplatePortalDirective } from '../portal-directives.d-C698lRc2.js';
import { ApplicationRef, Injector, ComponentRef, EmbeddedViewRef } from '@angular/core';

/**
 * A PortalOutlet for attaching portals to an arbitrary DOM element outside of the Angular
 * application context.
 */
declare class DomPortalOutlet extends BasePortalOutlet {
    /** Element into which the content is projected. */
    outletElement: Element;
    private _appRef?;
    private _defaultInjector?;
    private _document;
    /**
     * @param outletElement Element into which the content is projected.
     * @param _unusedComponentFactoryResolver Used to resolve the component factory.
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
    outletElement: Element, 
    /**
     * @deprecated No longer in use. To be removed.
     * @breaking-change 18.0.0
     */
    _unusedComponentFactoryResolver?: any, _appRef?: ApplicationRef | undefined, _defaultInjector?: Injector | undefined, 
    /**
     * @deprecated `_document` Parameter to be made required.
     * @breaking-change 10.0.0
     */
    _document?: any);
    /**
     * Attach the given ComponentPortal to DOM element.
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
 * @deprecated Use `DomPortalOutlet` instead.
 * @breaking-change 9.0.0
 */
declare class DomPortalHost extends DomPortalOutlet {
}

/**
 * Custom injector to be used when providing custom
 * injection tokens to components inside a portal.
 * @docs-private
 * @deprecated Use `Injector.create` instead.
 * @breaking-change 11.0.0
 */
declare class PortalInjector implements Injector {
    private _parentInjector;
    private _customTokens;
    constructor(_parentInjector: Injector, _customTokens: WeakMap<any, any>);
    get(token: any, notFoundValue?: any): any;
}

export { BasePortalOutlet, ComponentPortal, DomPortal, DomPortalHost, DomPortalOutlet, PortalInjector, TemplatePortal };
