import { Slot, VNodeArrayChildren, VNodeChild } from 'vue';
/**
 * We shouldn't use the following functions with slot flags `_: 1, 2, 3`
 */
export declare function resolveSlot(slot: Slot | undefined, fallback: () => VNodeArrayChildren): VNodeArrayChildren;
export declare function resolveSlotWithProps<T>(slot: Slot | undefined, props: T, fallback: (props: T) => VNodeArrayChildren): VNodeArrayChildren;
/**
 * Resolve slot with wrapper if content exists, no fallback
 */
export declare function resolveWrappedSlot(slot: Slot | undefined, wrapper: (children: VNodeArrayChildren | null) => VNodeChild): VNodeChild;
export declare function isSlotEmpty(slot: Slot | undefined): boolean;
