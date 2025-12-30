import { NavController } from '../../providers/nav-controller';
import { IonRouterOutlet } from './ion-router-outlet';
export declare class IonBackButtonDelegate {
    private routerOutlet;
    private navCtrl;
    defaultHref: string | undefined | null;
    constructor(routerOutlet: IonRouterOutlet, navCtrl: NavController);
    /**
     * @internal
     */
    onClick(ev: Event): void;
}
