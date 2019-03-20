import { LocationStrategy } from '@angular/common';
import { ElementRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { RouterDirection } from '@ionic/core';
import { NavController } from '../../providers/nav-controller';
export declare class RouterLinkDelegate {
    private locationStrategy;
    private navCtrl;
    private elementRef;
    private router;
    private routerLink?;
    private subscription?;
    routerDirection: RouterDirection;
    constructor(locationStrategy: LocationStrategy, navCtrl: NavController, elementRef: ElementRef, router: Router, routerLink?: RouterLink | undefined);
    ngOnInit(): void;
    ngOnChanges(): any;
    ngOnDestroy(): any;
    private updateTargetUrlAndHref;
    /**
     * @internal
     */
    onClick(ev: UIEvent): void;
}
