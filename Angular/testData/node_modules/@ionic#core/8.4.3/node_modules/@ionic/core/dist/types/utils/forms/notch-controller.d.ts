type NotchElement = HTMLIonInputElement | HTMLIonSelectElement | HTMLIonTextareaElement;
/**
 * A utility to calculate the size of an outline notch
 * width relative to the content passed. This is used in
 * components such as `ion-select` with `fill="outline"`
 * where we need to pass slotted HTML content. This is not
 * needed when rendering plaintext content because we can
 * render the plaintext again hidden with `opacity: 0` inside
 * of the notch. As a result we can rely on the intrinsic size
 * of the element to correctly compute the notch width. We
 * cannot do this with slotted content because we cannot project
 * it into 2 places at once.
 *
 * @internal
 * @param el: The host element
 * @param getNotchSpacerEl: A function that returns a reference to the notch spacer element inside of the component template.
 * @param getLabelSlot: A function that returns a reference to the slotted content.
 */
export declare const createNotchController: (el: NotchElement, getNotchSpacerEl: () => HTMLElement | undefined, getLabelSlot: () => Element | null) => NotchController;
export type NotchController = {
    calculateNotchWidth: () => void;
    destroy: () => void;
};
export {};
