import { ElementRef } from '@angular/core';
import { NavController } from '../../providers/nav-controller';
import { IonRouterOutlet } from './ion-router-outlet';
export declare class IonBackButtonDelegate {
    private routerOutlet;
    private navCtrl;
    private elementRef;
    defaultHref: string | undefined | null;
    constructor(routerOutlet: IonRouterOutlet, navCtrl: NavController, elementRef: ElementRef);
    onClick(ev: Event): void;
}
