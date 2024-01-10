import { AbstractControl } from '@angular/forms';
import { IEvoControlState } from './evo-control-state.interface';
export declare class EvoControlStateManager {
    control: AbstractControl;
    constructor(control: AbstractControl, extraStates?: IEvoControlState);
    readonly currentState: IEvoControlState;
}
