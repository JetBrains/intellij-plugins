/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'
import MyCounter from './MyCounter.vue'

const items = [1, 2, 3]
const counterRefs = ref<InstanceType<typeof MyCounter>[]>()
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
const __VLS_0 = __VLS_tryAsConstant((__VLS_unwrap(items, {} as import('vue').Ref<unknown>)));
for (const [item] of __VLS_vFor(__VLS_nonNull(__VLS_0))) {
const __VLS_1 = MyCounter;
// @ts-ignore
const __VLS_2 = __VLS_asFunctionalComponent1(__VLS_1, new __VLS_1({
// @ts-ignore
key: (item), count: (item), ref: "counterRefs", }));
const __VLS_3 = __VLS_2({
key: (item),
count: (item),
ref: "counterRefs",
}, ...__VLS_functionalComponentArgsRest(__VLS_2));
// @ts-ignore
[items,];
}
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
