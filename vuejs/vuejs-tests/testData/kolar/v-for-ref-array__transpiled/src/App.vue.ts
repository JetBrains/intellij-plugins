/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'

const items = [1, 2, 3]
const itemRefs = ref<HTMLDivElement[]>()
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
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
for (const [item] of __VLS_vFor((__VLS_ctx.items)!)) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
key: (item),
ref: "itemRefs",
});
( item );
// @ts-ignore
[items,];
}
type __VLS_TemplateRefs = {}
& { itemRefs: __VLS_Elements['div'][] };
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
