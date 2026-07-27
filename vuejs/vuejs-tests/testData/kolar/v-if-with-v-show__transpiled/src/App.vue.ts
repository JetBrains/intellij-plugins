/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'

const showIf = ref(true)
const showShow = ref(true)
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
showIf: typeof showIf;
showShow: typeof showShow;
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
return __VLS_ctx.showIf = !__VLS_ctx.showIf;
// @ts-ignore
[showIf,showIf,];
}},
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (...[$event]) => {
return __VLS_ctx.showShow = !__VLS_ctx.showShow;
// @ts-ignore
[showShow,showShow,];
}},
});
if (__VLS_ctx.showIf) {
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
});
__VLS_asFunctionalDirective(__VLS_directives.vShow, {} as import('vue').ObjectDirective)(null!, { ...__VLS_directiveBindingRestFields, value: (__VLS_ctx.showShow) }, null!, null!);
}
// @ts-ignore
[showIf,showShow,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
