import { ElementRef, NgZone, ChangeDetectorRef } from '@angular/core';
import { IonBackButton as IonBackButtonBase, NavController, Config } from '@ionic/angular/common';
import { IonRouterOutlet } from './router-outlet';
import * as i0 from "@angular/core";
export declare class IonBackButton extends IonBackButtonBase {
    constructor(routerOutlet: IonRouterOutlet, navCtrl: NavController, config: Config, r: ElementRef, z: NgZone, c: ChangeDetectorRef);
    static ɵfac: i0.ɵɵFactoryDeclaration<IonBackButton, [{ optional: true; }, null, null, null, null, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<IonBackButton, "ion-back-button", never, {}, {}, never, ["*"], true, never>;
}
