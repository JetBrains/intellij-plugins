/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'
import Base from './Base.vue'

const baseRef = ref<InstanceType<typeof Base>>()
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = import('vue').ShallowUnwrapRef<{
Base: typeof Base;
}>;
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
void __VLS_ctx, __VLS_components, __VLS_intrinsics, __VLS_directives;
const __VLS_template = () => {
const __VLS_0 = Base;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
// @ts-ignore
ref: "baseRef", ...{ class: "extra" }, }));
const __VLS_2 = __VLS_1({
ref: "baseRef",
...{ class: "extra" },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
/** @type {__VLS_StyleScopedClasses['extra']} */;
return {
};
};
void [__VLS_template];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
