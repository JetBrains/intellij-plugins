import type { Components, JSX } from "../dist/types/components";

interface IonPickerColumnOption extends Components.IonPickerColumnOption, HTMLElement {}
export const IonPickerColumnOption: {
    prototype: IonPickerColumnOption;
    new (): IonPickerColumnOption;
};
/**
 * Used to define this component and all nested components recursively.
 */
export const defineCustomElement: () => void;
