/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

type __VLS_Slots = {
  default(props: { msg: string }): any
};
const __VLS_slots = defineSlots<__VLS_Slots>()
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
__VLS_asFunctionalSlot(__VLS_slots['default'])({
msg: ('hello'),
});
const __VLS_base = (await import('vue')).defineComponent({
});
const __VLS_export = {} as __VLS_WithSlots<typeof __VLS_base, __VLS_Slots>;
export default {} as typeof __VLS_export;
type __VLS_WithSlots<T, S> = T & {
	new(): {
		$slots: S;
	}
};
