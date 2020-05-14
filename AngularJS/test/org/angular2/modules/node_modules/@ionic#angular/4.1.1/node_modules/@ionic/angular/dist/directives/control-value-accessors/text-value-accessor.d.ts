import { ElementRef } from '@angular/core';
import { ValueAccessor } from './value-accessor';
export declare class TextValueAccessor extends ValueAccessor {
    constructor(el: ElementRef);
    _handleInputEvent(value: any): void;
}
