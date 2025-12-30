export declare const watchForOptions: <T extends HTMLElement>(containerEl: HTMLElement, tagName: string, onChange: (el: T | undefined) => void) => MutationObserver | undefined;
/**
 * The "value" key is only set on some components such as ion-select-option.
 * As a result, we create a default union type of HTMLElement and the "value" key.
 * However, implementers are required to provide the appropriate component type
 * such as HTMLIonSelectOptionElement.
 */
export declare const findCheckedOption: <T extends HTMLElement & {
    value?: any | null;
}>(node: Node, tagName: string) => T | undefined;
