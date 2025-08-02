import { Location } from '@angular/common';
import { ViewContainerRef, ElementRef, NgZone } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { IonRouterOutlet as IonRouterOutletBase } from '@ionic/angular/common';
import * as i0 from "@angular/core";
export declare class IonRouterOutlet extends IonRouterOutletBase {
    readonly parentOutlet?: IonRouterOutlet | undefined;
    /**
     * `static: true` must be set so the query results are resolved
     * before change detection runs. Otherwise, the view container
     * ref will be ion-router-outlet instead of ng-container, and
     * the first view will be added as a sibling of ion-router-outlet
     * instead of a child.
     */
    outletContent: ViewContainerRef;
    /**
     * We need to pass in the correct instance of IonRouterOutlet
     * otherwise parentOutlet will be null in a nested outlet context.
     * This results in APIs such as NavController.pop not working
     * in nested outlets because the parent outlet cannot be found.
     */
    constructor(name: string, tabs: string, commonLocation: Location, elementRef: ElementRef, router: Router, zone: NgZone, activatedRoute: ActivatedRoute, parentOutlet?: IonRouterOutlet | undefined);
    static ɵfac: i0.ɵɵFactoryDeclaration<IonRouterOutlet, [{ attribute: "name"; }, { attribute: "tabs"; optional: true; }, null, null, null, null, null, { optional: true; skipSelf: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<IonRouterOutlet, "ion-router-outlet", never, {}, {}, never, ["*"], true, never>;
}
