/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import MyCounter from './MyCounter.vue'
import MyLabel from './MyLabel.vue'

const showCounter = true
const Dynamic = showCounter ? MyCounter : MyLabel
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
// @ts-ignore
__VLS_withDotValue(Dynamic, {} as import('vue').Ref<unknown>);
if (Dynamic.value) {
const __VLS_0 = (Dynamic.value);
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
// @ts-ignore
}));
const __VLS_2 = __VLS_1({
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
}
// @ts-ignore
[Dynamic,Dynamic,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
