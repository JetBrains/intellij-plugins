interface _GlobalComponents {
  AppLogo: typeof import("./app-logo.vue")['default']
}

declare module 'vue' {
  export interface GlobalComponents extends _GlobalComponents { }
}

export {}
