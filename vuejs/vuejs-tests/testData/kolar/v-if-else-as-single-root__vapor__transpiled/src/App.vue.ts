/// <reference types="@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="@vue/language-core/types/props-fallback.d.ts" />

import { ref } from 'vue'
import Base from './Base.vue'

const __VLS_export = ((
	__VLS_props: NonNullable<Awaited<typeof __VLS_setup>>['props'],
	__VLS_ctx?: __VLS_PrettifyLocal<Pick<NonNullable<Awaited<typeof __VLS_setup>>, 'attrs' | 'emit' | 'slots'>>,
	__VLS_exposed?: NonNullable<Awaited<typeof __VLS_setup>>['expose'],
	__VLS_setup = (async () => {
const baseRef = ref<InstanceType<typeof Base>>()
// @ts-ignore
declare const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, }: typeof import('vue');
const __VLS_ctx = {} as import('vue').ComponentPublicInstance;
type __VLS_LocalComponents = {};
type __VLS_GlobalComponents = import('vue').GlobalComponents;
let __VLS_components!: __VLS_LocalComponents & __VLS_GlobalComponents;
let __VLS_intrinsics!: import('vue/jsx-runtime').JSX.IntrinsicElements;
type __VLS_LocalDirectives = {};
let __VLS_directives!: __VLS_LocalDirectives & import('vue').GlobalDirectives;
const __VLS_0 = Base;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
ref: "baseRef",
...{ class: "extra" },
}));
const __VLS_2 = __VLS_1({
ref: "baseRef",
...{ class: "extra" },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
var __VLS_5!: Parameters<NonNullable<typeof __VLS_3['expose']>>[0];
/** @type {__VLS_StyleScopedClasses['extra']} */;
var __VLS_3!: __VLS_ExtractComponentContext<typeof __VLS_0, typeof __VLS_2>;
// @ts-ignore
var __VLS_6 = __VLS_5, ;
type __VLS_TemplateRefs = {}
& { baseRef: typeof __VLS_6 | null };
type __VLS_RootEl = 
| NonNullable<typeof __VLS_5>['$el'];
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
