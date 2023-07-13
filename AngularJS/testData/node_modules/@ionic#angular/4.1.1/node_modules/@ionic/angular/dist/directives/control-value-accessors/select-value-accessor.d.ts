import { ElementRef } from '@angular/core';
import { ValueAccessor } from './value-accessor';
export declare class SelectValueAccessor extends ValueAccessor {
    constructor(el: ElementRef);
    _handleChangeEvent(value: any): void;
}
