import type { Components, JSX } from "../dist/types/components";

interface IonSegmentContent extends Components.IonSegmentContent, HTMLElement {}
export const IonSegmentContent: {
    prototype: IonSegmentContent;
    new (): IonSegmentContent;
};
/**
 * Used to define this component and all nested components recursively.
 */
export const defineCustomElement: () => void;
