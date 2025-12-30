/**
 * Used to update a scoped component that uses emulated slots. This fires when
 * content is passed into the slot or when the content inside of a slot changes.
 * This is not needed for components using native slots in the Shadow DOM.
 * @internal
 * @param el The host element to observe
 * @param slotName mutationCallback will fire when nodes on these slot(s) change
 * @param mutationCallback The callback to fire whenever the slotted content changes
 */
export declare const createSlotMutationController: (el: HTMLElement, slotName: string | string[], mutationCallback: () => void) => SlotMutationController;
export type SlotMutationController = {
    destroy: () => void;
};
