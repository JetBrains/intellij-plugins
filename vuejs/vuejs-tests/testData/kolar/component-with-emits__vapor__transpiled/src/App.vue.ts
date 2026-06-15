                        
                                       
                                   

             
         

          
             
  
                                             
                                
           /// <reference types="../node_modules/@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="../node_modules/@vue/language-core/types/props-fallback.d.ts" />

import MyCounter from './MyCounter.vue'
import MyLabel from './MyLabel.vue'

const __VLS_export = ((
	__VLS_props: NonNullable<Awaited<typeof __VLS_setup>>['props'],
	__VLS_ctx?: __VLS_PrettifyLocal<Pick<NonNullable<Awaited<typeof __VLS_setup>>, 'attrs' | 'emit' | 'slots'>>,
	__VLS_exposed?: NonNullable<Awaited<typeof __VLS_setup>>['expose'],
	__VLS_setup = (async () => {
let count = 1
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
...{ 'onChange': {} as any },
}));
const __VLS_2 = __VLS_1({
...{ 'onChange': {} as any },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
let __VLS_5!: __VLS_ResolveEmits<typeof __VLS_0, typeof __VLS_3.emit>;
const __VLS_6: __VLS_ResolveEvent<typeof __VLS_4, typeof __VLS_5, 'onChange', 'change', 'change'> = {
/** @type {typeof __VLS_5.change} */
onChange: (...[$event]) => {
__VLS_ctx.count = $event.value;
// @ts-ignore
[count,count,];
},
};
var __VLS_3!: __VLS_FunctionalComponentCtx<typeof __VLS_0, typeof __VLS_2>;
var __VLS_4!: __VLS_FunctionalComponentProps<typeof __VLS_0, typeof __VLS_2>;
const __VLS_7 = MyLabel;
// @ts-ignore
const __VLS_8 = __VLS_asFunctionalComponent1(__VLS_7, new __VLS_7({
...{ 'onSelect': {} as any },
}));
const __VLS_9 = __VLS_8({
...{ 'onSelect': {} as any },
}, ...__VLS_functionalComponentArgsRest(__VLS_8));
let __VLS_12!: __VLS_ResolveEmits<typeof __VLS_7, typeof __VLS_10.emit>;
const __VLS_13: __VLS_ResolveEvent<typeof __VLS_11, typeof __VLS_12, 'onSelect', 'select', 'select'> = {
/** @type {typeof __VLS_12.select} */
onSelect: (...[$event]) => {
__VLS_ctx.count = 0;
// @ts-ignore
[count,];
},
};
var __VLS_10!: __VLS_FunctionalComponentCtx<typeof __VLS_7, typeof __VLS_9>;
var __VLS_11!: __VLS_FunctionalComponentProps<typeof __VLS_7, typeof __VLS_9>;
// @ts-ignore
[];
return {} as {
	props: import('vue').PublicProps & (typeof globalThis extends { __VLS_PROPS_FALLBACK: infer P } ? P : {});
	expose: (exposed: {}) => void;
	attrs: any;
	slots: {};
	emit: {};
};
})(),
) => ({} as import('vue').VNode & { __ctx?: Awaited<typeof __VLS_setup> }));
export default {} as typeof __VLS_export;
type __VLS_PrettifyLocal<T> = (T extends any ? { [K in keyof T]: T[K]; } : { [K in keyof T as K]: T[K]; }) & {};
