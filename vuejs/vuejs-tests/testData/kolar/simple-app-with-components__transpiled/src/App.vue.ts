                        
                                       
                                   

               
         

          
             
  
              
            
           /// <reference types="../node_modules/@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="../node_modules/@vue/language-core/types/props-fallback.d.ts" />

import MyCounter from './MyCounter.vue'
import MyLabel from './MyLabel.vue'

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
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
