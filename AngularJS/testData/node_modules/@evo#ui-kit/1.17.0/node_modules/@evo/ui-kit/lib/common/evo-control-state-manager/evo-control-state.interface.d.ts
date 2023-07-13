import { EvoControlStates } from './evo-control-states.enum';
export interface IEvoControlState {
    [EvoControlStates.valid]?: boolean;
    [EvoControlStates.invalid]?: boolean;
}
