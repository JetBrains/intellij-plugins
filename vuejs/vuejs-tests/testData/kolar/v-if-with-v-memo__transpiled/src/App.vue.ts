/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'

const show = ref(true)
const items = [1, 2, 3]
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
show: typeof show;
items: typeof items;
}>;
const __VLS_ctx = {
...{} as import('vue').ComponentPublicInstance,
...{} as __VLS_SetupExposed,
};
type __VLS_LocalComponents = __VLS_SetupExposed;
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = __VLS_SetupExposed;
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (...[$event]) => {
return __VLS_ctx.show = !__VLS_ctx.show;
// @ts-ignore
[show,show,];
}},
});
if (__VLS_ctx.show) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
__VLS_asFunctionalDirective(__VLS_directives.vMemo, {} as import('vue').ObjectDirective)(null!, { ...__VLS_directiveBindingRestFields, value: ([__VLS_ctx.items.length]) }, null!, null!);
( __VLS_ctx.items.length );
}
// @ts-ignore
[show,items,items,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
