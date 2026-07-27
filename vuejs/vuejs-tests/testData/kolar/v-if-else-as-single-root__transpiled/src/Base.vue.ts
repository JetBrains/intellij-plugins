/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

const show = true
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
show: typeof show;
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
if (__VLS_ctx.show) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
...{ class: "on" },
});
/** @type {__VLS_StyleScopedClasses['on']} */;
}
else {
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
...{ class: "off" },
});
/** @type {__VLS_StyleScopedClasses['off']} */;
}
type __VLS_RootEl = 
| __VLS_Elements['div']
| __VLS_Elements['span'];
// @ts-ignore
[show,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
