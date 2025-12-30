import { ElementRef, NgZone, ChangeDetectorRef } from '@angular/core';
import type { Components } from '@ionic/core';
import { Config } from '../../providers/config';
import { NavController } from '../../providers/nav-controller';
import { IonRouterOutlet } from './router-outlet';
import * as i0 from "@angular/core";
export declare interface IonBackButton extends Components.IonBackButton {
}
export declare class IonBackButton {
    private routerOutlet;
    private navCtrl;
    private config;
    private r;
    protected z: NgZone;
    protected el: HTMLElement;
    constructor(routerOutlet: IonRouterOutlet, navCtrl: NavController, config: Config, r: ElementRef, z: NgZone, c: ChangeDetectorRef);
    /**
     * @internal
     */
    onClick(ev: Event): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<IonBackButton, [{ optional: true; }, null, null, null, null, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<IonBackButton, never, never, { "color": { "alias": "color"; "required": false; }; "defaultHref": { "alias": "defaultHref"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "icon": { "alias": "icon"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "routerAnimation": { "alias": "routerAnimation"; "required": false; }; "text": { "alias": "text"; "required": false; }; "type": { "alias": "type"; "required": false; }; }, {}, never, never, false, never>;
}
