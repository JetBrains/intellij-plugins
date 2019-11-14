import { EventEmitter } from '@angular/core';
import { NavController } from '../../providers/nav-controller';
import { IonTabBar } from '../proxies';
import { IonRouterOutlet } from './ion-router-outlet';
import { StackEvent } from './stack-utils';
export declare class IonTabs {
    private navCtrl;
    outlet: IonRouterOutlet;
    tabBar: IonTabBar | undefined;
    ionTabsWillChange: EventEmitter<{
        tab: string;
    }>;
    ionTabsDidChange: EventEmitter<{
        tab: string;
    }>;
    constructor(navCtrl: NavController);
    /**
     * @internal
     */
    onPageSelected(detail: StackEvent): void;
    select(tab: string): Promise<boolean>;
    getSelected(): string | undefined;
}
