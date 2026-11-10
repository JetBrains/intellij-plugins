/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import MyCounter from './MyCounter.vue'

const __VLS_export = ((
	__VLS_props: NonNullable<Awaited<typeof __VLS_setup>>['props'],
	__VLS_ctx?: __VLS_PrettifyLocal<Pick<NonNullable<Awaited<typeof __VLS_setup>>, 'attrs' | 'emit' | 'slots'>>,
	__VLS_exposed?: NonNullable<Awaited<typeof __VLS_setup>>['expose'],
	__VLS_setup = (async () => {
const names = ['header', 'footer'] as const
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
const __VLS_0 = MyCounter || MyCounter;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
// @ts-ignore
}));
const __VLS_2 = __VLS_1({
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
var __VLS_3!: __VLS_ExtractComponentContext<typeof __VLS_0, typeof __VLS_2>;
const { default: __VLS_5 } = __VLS_nonNull(__VLS_3.slots);
const __VLS_6 = __VLS_tryAsConstant((__VLS_unwrap(names, {} as import('vue').Ref<unknown>)));
for (const [name] of __VLS_vFor(__VLS_nonNull(__VLS_6))) {
{
const { [__VLS_tryAsConstant(name)]: __VLS_7 } = __VLS_nonNull(__VLS_3.slots);
const [slotProps] = __VLS_vSlot(__VLS_nonNull(__VLS_7));
( name );
( slotProps.value );
// @ts-ignore
[names,];
}
// @ts-ignore
[];
}
// @ts-ignore
[];
// @ts-ignore
[];
return {
};
};
void [__VLS_template];
return {} as {
	props: import('vue').PublicProps & (typeof globalThis extends { __VLS_PROPS_FALLBACK: infer P } ? P : {});
	expose: (exposed: {}) => void;
	attrs: any;
	slots: {};
	emit: {};
};
})(),
) => ({} as import('vue').VNode & { __ctx?: NonNullable<Awaited<typeof __VLS_setup>> }));
export default {} as typeof __VLS_export;
type __VLS_PrettifyLocal<T> = (T extends any ? { [K in keyof T]: T[K]; } : { [K in keyof T as K]: T[K]; }) & {};
