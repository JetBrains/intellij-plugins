import type { Components, JSX } from "../dist/types/components";

interface IonSelectModal extends Components.IonSelectModal, HTMLElement {}
export const IonSelectModal: {
    prototype: IonSelectModal;
    new (): IonSelectModal;
};
/**
 * Used to define this component and all nested components recursively.
 */
export const defineCustomElement: () => void;
