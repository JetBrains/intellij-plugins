import { OverlayBaseController } from '@ionic/angular/common';
import type { ModalOptions } from '@ionic/core';
import * as i0 from "@angular/core";
export declare class ModalController extends OverlayBaseController<ModalOptions, HTMLIonModalElement> {
    private angularDelegate;
    private injector;
    private environmentInjector;
    constructor();
    create(opts: ModalOptions): Promise<HTMLIonModalElement>;
    static ɵfac: i0.ɵɵFactoryDeclaration<ModalController, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ModalController>;
}
