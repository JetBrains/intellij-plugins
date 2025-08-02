import { QueryList } from '@angular/core';
import { IonTabs as IonTabsBase } from '@ionic/angular/common';
import { IonTabBar, IonTab } from '../proxies';
import { IonRouterOutlet } from './ion-router-outlet';
import * as i0 from "@angular/core";
export declare class IonTabs extends IonTabsBase {
    outlet: IonRouterOutlet;
    tabBar: IonTabBar | undefined;
    tabBars: QueryList<IonTabBar>;
    tabs: QueryList<IonTab>;
    static ɵfac: i0.ɵɵFactoryDeclaration<IonTabs, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<IonTabs, "ion-tabs", never, {}, {}, ["tabBar", "tabBars", "tabs"], ["[slot=top]", "ion-tab", "*"], false, never>;
}
