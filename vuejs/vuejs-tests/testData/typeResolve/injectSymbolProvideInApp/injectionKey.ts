import type {InjectionKey} from "vue";

export enum Lang {
  EN,
  DE,
}

export const myInjectionKey = Symbol() as InjectionKey<{ name?: string, lang?: Lang }>