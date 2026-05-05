export {}
// for vue directives auto import
declare module 'vue' {
  interface GlobalDirectives {
    vMyClickOutside: typeof import('my-vue-global-directives')["myClickOutside"]
    vMyIntersect: typeof import('my-vue-global-directives')["myIntersect"]
    vMyMutate: typeof import('my-vue-global-directives')["myMutate"]
  }
}