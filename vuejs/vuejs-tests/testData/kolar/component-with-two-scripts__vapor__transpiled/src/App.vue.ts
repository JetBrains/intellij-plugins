/// <reference types="../node_modules/@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="../node_modules/@vue/language-core/types/props-fallback.d.ts" />

import MyCounter from './MyCounter.vue'
import MyLabel from './MyLabel.vue'


export interface MyUser {
  name: string
  age: number
}

export type OtherUser = Readonly<{
  name: string
  age: number
}>
export default {} as typeof __VLS_export;
const __VLS_export = ((
	__VLS_props: NonNullable<Awaited<typeof __VLS_setup>>['props'],
	__VLS_ctx?: __VLS_PrettifyLocal<Pick<NonNullable<Awaited<typeof __VLS_setup>>, 'attrs' | 'emit' | 'slots'>>,
	__VLS_exposed?: NonNullable<Awaited<typeof __VLS_setup>>['expose'],
	__VLS_setup = (async () => {
const count = 1
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
type __VLS_SetupExposed = import('vue').ShallowUnwrapRef<{
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
( __VLS_ctx.count );
const __VLS_0 = MyCounter;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
}));
const __VLS_2 = __VLS_1({
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
const __VLS_5 = MyLabel;
// @ts-ignore
const __VLS_6 = __VLS_asFunctionalComponent1(__VLS_5, new __VLS_5({
}));
const __VLS_7 = __VLS_6({
}, ...__VLS_functionalComponentArgsRest(__VLS_6));
// @ts-ignore
[count,];
return {} as {
	props: import('vue').PublicProps & (typeof globalThis extends { __VLS_PROPS_FALLBACK: infer P } ? P : {});
	expose: (exposed: {}) => void;
	attrs: any;
	slots: {};
	emit: {};
};
})(),
) => ({} as import('vue').VNode & { __ctx?: NonNullable<Awaited<typeof __VLS_setup>> }));
type __VLS_PrettifyLocal<T> = (T extends any ? { [K in keyof T]: T[K]; } : { [K in keyof T as K]: T[K]; }) & {};
