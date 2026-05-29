                        
               
         

          
                          
                           
             
                            
                             
           
/// <reference types="../node_modules/@vue/language-core/types/template-helpers.d.ts" />
/// <reference types="../node_modules/@vue/language-core/types/props-fallback.d.ts" />

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
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
ref: "first",
});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
ref: "second",
});
( __VLS_ctx.count );
( __VLS_ctx.$refs.first.tabIndex );
( __VLS_ctx.$refs.second.tabIndex );
type __VLS_TemplateRefs = {}
& { first: __VLS_Elements['div'] }
& { second: __VLS_Elements['div'] };
// @ts-ignore
[count,$refs,$refs,];
const __VLS_export = (await import('vue')).defineComponent({
});
export default {} as typeof __VLS_export;
