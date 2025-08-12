import { ElementRef, Injector } from '@angular/core';
import { ValueAccessor } from '@ionic/angular/common';
import * as i0 from "@angular/core";
export declare class SelectValueAccessorDirective extends ValueAccessor {
    constructor(injector: Injector, el: ElementRef);
    _handleChangeEvent(el: HTMLIonSelectElement | HTMLIonRadioGroupElement | HTMLIonSegmentElement | HTMLIonDatetimeElement): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<SelectValueAccessorDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<SelectValueAccessorDirective, "ion-select, ion-radio-group, ion-segment, ion-datetime", never, {}, {}, never, never, false, never>;
}
