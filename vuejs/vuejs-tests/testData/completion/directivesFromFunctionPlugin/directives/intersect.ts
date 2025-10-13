import type {DirectiveBinding} from "vue";

export interface ObserveDirectiveBinding extends Omit<DirectiveBinding, 'value'> {
  modifiers: {
    once?: boolean;
    quiet?: boolean;
    passive?: boolean;
  };
}


export const intersect = {
  mounted(el: Element, binding: ObserveDirectiveBinding) {
    console.log("Intersect: ", el, binding)
  },
}
