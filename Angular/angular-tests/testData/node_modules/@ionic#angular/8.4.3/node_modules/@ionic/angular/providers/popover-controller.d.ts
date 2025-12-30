import { OverlayBaseController } from '@ionic/angular/common';
import type { PopoverOptions } from '@ionic/core';
export declare class PopoverController extends OverlayBaseController<PopoverOptions, HTMLIonPopoverElement> {
    private angularDelegate;
    private injector;
    private environmentInjector;
    constructor();
    create(opts: PopoverOptions): Promise<HTMLIonPopoverElement>;
}
