import { ElementRef } from '@angular/core';
import { ValueAccessor } from './value-accessor';
export declare class BooleanValueAccessor extends ValueAccessor {
    constructor(el: ElementRef);
    writeValue(value: any): void;
    _handleIonChange(value: any): void;
}
