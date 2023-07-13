import { ElementRef } from '@angular/core';
import { ValueAccessor } from './value-accessor';
export declare class NumericValueAccessor extends ValueAccessor {
    constructor(el: ElementRef);
    _handleIonChange(value: any): void;
    registerOnChange(fn: (_: number | null) => void): void;
}
