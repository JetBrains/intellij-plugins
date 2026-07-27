/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const items = ['a', 'b', 'c']
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
__VLS_asFunctionalElement1(__VLS_intrinsics.dl, __VLS_intrinsics.dl)({
});
for (const [item] of __VLS_vFor((__VLS_ctx.items)!)) {
__VLS_asFunctionalElement(__VLS_intrinsics.template)({
key: (item),
});
__VLS_asFunctionalElement1(__VLS_intrinsics.dt, __VLS_intrinsics.dt)({
});
( item );
__VLS_asFunctionalElement1(__VLS_intrinsics.dd, __VLS_intrinsics.dd)({
});
( item.length );
// @ts-ignore
[items,];
}
type __VLS_RootEl = 
| __VLS_Elements['dl'];
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
