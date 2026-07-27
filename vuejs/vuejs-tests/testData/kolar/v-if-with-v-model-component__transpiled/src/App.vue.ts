/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'
import MyCounter from './MyCounter.vue'

const show = ref(true)
const count = ref(0)
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
show: typeof show;
count: typeof count;
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
const __VLS_0 = MyCounter;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
count: (__VLS_ctx.count),
}));
const __VLS_2 = __VLS_1({
count: (__VLS_ctx.count),
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
}
// @ts-ignore
[show,count,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
