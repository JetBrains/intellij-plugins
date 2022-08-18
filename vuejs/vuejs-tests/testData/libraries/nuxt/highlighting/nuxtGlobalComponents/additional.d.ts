// additional declarations of GlobalComponents to ensure that we merge them + test regressions
declare module 'vue' {
  export interface GlobalComponents {
    'My1': typeof import("./My1.vue")['default'],
  }
}

declare module '@vue/runtime-core' {
  export interface GlobalComponents {
    'My2': typeof import("./My2.vue")['default'],
  }
}

declare module '@vue/runtime-dom' {
  export interface GlobalComponents {
    'My3': typeof import("./My3.vue")['default'],
  }
}

export {}
