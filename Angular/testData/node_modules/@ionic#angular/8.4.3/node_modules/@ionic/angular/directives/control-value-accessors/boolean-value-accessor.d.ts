import { ElementRef, Injector } from '@angular/core';
import { ValueAccessor } from '@ionic/angular/common';
import * as i0 from "@angular/core";
export declare class BooleanValueAccessorDirective extends ValueAccessor {
    constructor(injector: Injector, el: ElementRef);
    writeValue(value: boolean): void;
    _handleIonChange(el: HTMLIonCheckboxElement | HTMLIonToggleElement): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<BooleanValueAccessorDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<BooleanValueAccessorDirective, "ion-checkbox,ion-toggle", never, {}, {}, never, never, false, never>;
}
