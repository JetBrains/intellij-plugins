import { SelectGroupOption, SelectBaseOption } from '../../select/src/interface';
export declare type AutoCompleteOption = SelectBaseOption<string, string>;
export interface AutoCompleteGroupOption extends Omit<SelectGroupOption, 'children'> {
    children: AutoCompleteOptions;
}
export declare type AutoCompleteOptions = Array<AutoCompleteOption | AutoCompleteGroupOption | string>;
export declare type OnUpdateValue = (value: string & (string | null)) => void;
export declare type OnUpdateImpl = (value: string | null) => void;
export declare type OnSelect = (value: string | number) => void;
export interface AutoCompleteInst {
    focus: () => void;
    blur: () => void;
}
