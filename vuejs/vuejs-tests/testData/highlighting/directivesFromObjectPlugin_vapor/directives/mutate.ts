export interface MutationOptions {
    attr?: boolean;
    char?: boolean;
    child?: boolean;
    sub?: boolean;
    once?: boolean;
    immediate?: boolean;
}

export function mutate(el: Element, _, __, options: MutationOptions) {
    console.log("Mutate: ", el, options)
}
