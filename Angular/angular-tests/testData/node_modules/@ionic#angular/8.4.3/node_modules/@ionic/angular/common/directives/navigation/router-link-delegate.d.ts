import { LocationStrategy } from '@angular/common';
import { ElementRef, OnChanges, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import type { AnimationBuilder, RouterDirection } from '@ionic/core/components';
import { NavController } from '../../providers/nav-controller';
import * as i0 from "@angular/core";
/**
 * Adds support for Ionic routing directions and animations to the base Angular router link directive.
 *
 * When the router link is clicked, the directive will assign the direction and
 * animation so that the routing integration will transition correctly.
 */
export declare class RouterLinkDelegateDirective implements OnInit, OnChanges {
    private locationStrategy;
    private navCtrl;
    private elementRef;
    private router;
    private routerLink?;
    routerDirection: RouterDirection;
    routerAnimation?: AnimationBuilder;
    constructor(locationStrategy: LocationStrategy, navCtrl: NavController, elementRef: ElementRef, router: Router, routerLink?: RouterLink | undefined);
    ngOnInit(): void;
    ngOnChanges(): void;
    /**
     * The `tabindex` is set to `0` by default on the host element when
     * the `routerLink` directive is used. This causes issues with Ionic
     * components that wrap an `a` or `button` element, such as `ion-item`.
     * See issue https://github.com/angular/angular/issues/28345
     *
     * This method removes the `tabindex` attribute from the host element
     * to allow the Ionic component to manage the focus state correctly.
     */
    private updateTabindex;
    private updateTargetUrlAndHref;
    /**
     * @internal
     */
    onClick(ev: UIEvent): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<RouterLinkDelegateDirective, [null, null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<RouterLinkDelegateDirective, ":not(a):not(area)[routerLink]", never, { "routerDirection": { "alias": "routerDirection"; "required": false; }; "routerAnimation": { "alias": "routerAnimation"; "required": false; }; }, {}, never, never, false, never>;
}
export declare class RouterLinkWithHrefDelegateDirective implements OnInit, OnChanges {
    private locationStrategy;
    private navCtrl;
    private elementRef;
    private router;
    private routerLink?;
    routerDirection: RouterDirection;
    routerAnimation?: AnimationBuilder;
    constructor(locationStrategy: LocationStrategy, navCtrl: NavController, elementRef: ElementRef, router: Router, routerLink?: RouterLink | undefined);
    ngOnInit(): void;
    ngOnChanges(): void;
    private updateTargetUrlAndHref;
    /**
     * @internal
     */
    onClick(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<RouterLinkWithHrefDelegateDirective, [null, null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<RouterLinkWithHrefDelegateDirective, "a[routerLink],area[routerLink]", never, { "routerDirection": { "alias": "routerDirection"; "required": false; }; "routerAnimation": { "alias": "routerAnimation"; "required": false; }; }, {}, never, never, false, never>;
}
