/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
var __VLS_0 = {
};
// @ts-ignore
var __VLS_1 = __VLS_0, ;
type __VLS_Slots = {}
& { default?: (props: typeof __VLS_1) => any };
type __VLS_RootEl = 
| __VLS_Elements['div'];
const __VLS_base = (await import('vue')).defineComponent({
});
const __VLS_export = {} as __VLS_WithSlots<typeof __VLS_base, __VLS_Slots>;
export default {} as typeof __VLS_export;
type __VLS_WithSlots<T, S> = T & {
	new(): {
		$slots: S;
	}
};
