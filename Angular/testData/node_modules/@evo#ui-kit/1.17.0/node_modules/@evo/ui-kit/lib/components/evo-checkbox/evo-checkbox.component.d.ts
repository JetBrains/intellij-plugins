import { ControlValueAccessor } from '@angular/forms';
import { EvoBaseControl } from '../../common/evo-base-control';
export declare class EvoCheckboxComponent extends EvoBaseControl implements ControlValueAccessor {
    disabled: boolean;
    private _value;
    onChange: (_: any) => void;
    onTouched: () => void;
    value: boolean;
    readonly checkboxClass: {
        'invalid': boolean;
    };
    writeValue(value: boolean): void;
    registerOnChange(fn: any): void;
    registerOnTouched(fn: any): void;
    setDisabledState(state: boolean): void;
}
