import type { ComponentInterface } from '../../stencil-public-runtime';
import type { SelectModalOption } from './select-modal-interface';
export declare class SelectModal implements ComponentInterface {
    el: HTMLIonSelectModalElement;
    header?: string;
    multiple?: boolean;
    options: SelectModalOption[];
    private closeModal;
    private findOptionFromEvent;
    private getValues;
    private callOptionHandler;
    private setChecked;
    private renderRadioOptions;
    private renderCheckboxOptions;
    render(): any;
}
