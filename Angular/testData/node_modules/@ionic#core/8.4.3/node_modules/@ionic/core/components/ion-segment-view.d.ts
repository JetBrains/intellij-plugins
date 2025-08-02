import type { Components, JSX } from "../dist/types/components";

interface IonSegmentView extends Components.IonSegmentView, HTMLElement {}
export const IonSegmentView: {
    prototype: IonSegmentView;
    new (): IonSegmentView;
};
/**
 * Used to define this component and all nested components recursively.
 */
export const defineCustomElement: () => void;
