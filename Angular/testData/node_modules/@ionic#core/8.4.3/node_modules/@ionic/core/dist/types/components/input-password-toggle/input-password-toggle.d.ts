import type { ComponentInterface } from '../../stencil-public-runtime';
import type { Color, TextFieldTypes } from '../../interface';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 */
export declare class InputPasswordToggle implements ComponentInterface {
    private inputElRef;
    el: HTMLIonInputElement;
    /**
     * The color to use from your application's color palette.
     * Default options are: `"primary"`, `"secondary"`, `"tertiary"`, `"success"`, `"warning"`, `"danger"`, `"light"`, `"medium"`, and `"dark"`.
     * For more information on colors, see [theming](/docs/theming/basics).
     */
    color?: Color;
    /**
     * The icon that can be used to represent showing a password. If not set, the "eye" Ionicon will be used.
     */
    showIcon?: string;
    /**
     * The icon that can be used to represent hiding a password. If not set, the "eyeOff" Ionicon will be used.
     */
    hideIcon?: string;
    /**
     * @internal
     */
    type: TextFieldTypes;
    /**
     * Whenever the input type changes we need to re-run validation to ensure the password
     * toggle is being used with the correct input type. If the application changes the type
     * outside of this component we also need to re-render so the correct icon is shown.
     */
    onTypeChange(newValue: TextFieldTypes): void;
    connectedCallback(): void;
    disconnectedCallback(): void;
    private togglePasswordVisibility;
    render(): any;
}
