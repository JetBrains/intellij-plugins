import type { Components, JSX } from "../dist/types/components";

interface IonPickerLegacy extends Components.IonPickerLegacy, HTMLElement {}
export const IonPickerLegacy: {
    prototype: IonPickerLegacy;
    new (): IonPickerLegacy;
};
/**
 * Used to define this component and all nested components recursively.
 */
export const defineCustomElement: () => void;
