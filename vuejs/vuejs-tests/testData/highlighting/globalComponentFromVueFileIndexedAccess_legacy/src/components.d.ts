interface _GlobalComponents {
  AppLogo: typeof import("./AppLogo.vue")['default']
}

declare module 'vue' {
  export interface GlobalComponents extends _GlobalComponents { }
}

export {}
