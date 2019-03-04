import { ElementRef } from '@angular/core';
import { ValueAccessor } from './value-accessor';
export declare class RadioValueAccessor extends ValueAccessor {
    constructor(el: ElementRef);
    _handleIonSelect(value: any): void;
}
