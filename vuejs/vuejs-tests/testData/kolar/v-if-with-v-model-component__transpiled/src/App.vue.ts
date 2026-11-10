/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'
import MyCounter from './MyCounter.vue'

const show = ref(true)
const count = ref(0)
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = import('vue').ShallowUnwrapRef<{
MyCounter: typeof MyCounter;
}>;
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
const __VLS_template = () => {
__VLS_withDotValue(show, {} as import('vue').Ref<unknown>);
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
...{ onClick: (...[$event]: __VLS_EventArgs<typeof __VLS_intrinsics['button'], 'onClick'>) => {
void $event;
return (show.value = !show.value);
// @ts-ignore
[show,show,];
}},
});
if (show.value) {
const __VLS_0 = MyCounter;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
// @ts-ignore
count: (__VLS_unwrap(count, {} as import('vue').Ref<unknown>)), }));
const __VLS_2 = __VLS_1({
count: (__VLS_unwrap(count, {} as import('vue').Ref<unknown>)),
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
}
// @ts-ignore
[show,count,];
return {
};
};
void [__VLS_template];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
