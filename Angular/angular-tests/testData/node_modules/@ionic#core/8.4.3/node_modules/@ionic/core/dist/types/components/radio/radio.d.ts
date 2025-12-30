import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { Color } from '../../interface';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 *
 * @slot - The label text to associate with the radio. Use the "labelPlacement" property to control where the label is placed relative to the radio.
 *
 * @part container - The container for the radio mark.
 * @part label - The label text describing the radio.
 * @part mark - The checkmark or dot used to indicate the checked state.
 */
export declare class Radio implements ComponentInterface {
    private inputId;
    private radioGroup;
    el: HTMLIonRadioElement;
    /**
     * If `true`, the radio is selected.
     */
    checked: boolean;
    /**
     * The tabindex of the radio button.
     * @internal
     */
    buttonTabindex: number;
    /**
     * The color to use from your application's color palette.
     * Default options are: `"primary"`, `"secondary"`, `"tertiary"`, `"success"`, `"warning"`, `"danger"`, `"light"`, `"medium"`, and `"dark"`.
     * For more information on colors, see [theming](/docs/theming/basics).
     */
    color?: Color;
    /**
     * The name of the control, which is submitted with the form data.
     */
    name: string;
    /**
     * If `true`, the user cannot interact with the radio.
     */
    disabled: boolean;
    /**
     * the value of the radio.
     */
    value?: any | null;
    valueChanged(): void;
    /**
     * Where to place the label relative to the radio.
     * `"start"`: The label will appear to the left of the radio in LTR and to the right in RTL.
     * `"end"`: The label will appear to the right of the radio in LTR and to the left in RTL.
     * `"fixed"`: The label has the same behavior as `"start"` except it also has a fixed width. Long text will be truncated with ellipses ("...").
     * `"stacked"`: The label will appear above the radio regardless of the direction. The alignment of the label can be controlled with the `alignment` property.
     */
    labelPlacement: 'start' | 'end' | 'fixed' | 'stacked';
    /**
     * How to pack the label and radio within a line.
     * `"start"`: The label and radio will appear on the left in LTR and
     * on the right in RTL.
     * `"end"`: The label and radio will appear on the right in LTR and
     * on the left in RTL.
     * `"space-between"`: The label and radio will appear on opposite
     * ends of the line with space between the two elements.
     * Setting this property will change the radio `display` to `block`.
     */
    justify?: 'start' | 'end' | 'space-between';
    /**
     * How to control the alignment of the radio and label on the cross axis.
     * `"start"`: The label and control will appear on the left of the cross axis in LTR, and on the right side in RTL.
     * `"center"`: The label and control will appear at the center of the cross axis in both LTR and RTL.
     * Setting this property will change the radio `display` to `block`.
     */
    alignment?: 'start' | 'center';
    /**
     * Emitted when the radio button has focus.
     */
    ionFocus: EventEmitter<void>;
    /**
     * Emitted when the radio button loses focus.
     */
    ionBlur: EventEmitter<void>;
    componentDidLoad(): void;
    /** @internal */
    setFocus(ev?: globalThis.Event): Promise<void>;
    /** @internal */
    setButtonTabindex(value: number): Promise<void>;
    connectedCallback(): void;
    disconnectedCallback(): void;
    private updateState;
    private onClick;
    private onFocus;
    private onBlur;
    private get hasLabel();
    private renderRadioControl;
    render(): any;
}
