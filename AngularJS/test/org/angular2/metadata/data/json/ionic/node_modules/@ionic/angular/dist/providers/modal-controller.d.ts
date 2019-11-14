import { ComponentFactoryResolver, Injector } from '@angular/core';
import { ModalOptions } from '@ionic/core';
import { OverlayBaseController } from '../util/overlay';
import { AngularDelegate } from './angular-delegate';
export declare class ModalController extends OverlayBaseController<ModalOptions, HTMLIonModalElement> {
    private angularDelegate;
    private resolver;
    private injector;
    constructor(angularDelegate: AngularDelegate, resolver: ComponentFactoryResolver, injector: Injector, doc: any);
    create(opts: ModalOptions): Promise<HTMLIonModalElement>;
}
