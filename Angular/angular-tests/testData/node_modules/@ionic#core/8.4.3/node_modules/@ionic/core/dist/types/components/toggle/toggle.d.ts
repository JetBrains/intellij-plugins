import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { Color } from '../../interface';
import type { ToggleChangeEventDetail } from './toggle-interface';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 *
 * @slot - The label text to associate with the toggle. Use the "labelPlacement" property to control where the label is placed relative to the toggle.
 *
 * @part track - The background track of the toggle.
 * @part handle - The toggle handle, or knob, used to change the checked state.
 * @part label - The label text describing the toggle.
 */
export declare class Toggle implements ComponentInterface {
    private inputId;
    private gesture?;
    private focusEl?;
    private lastDrag;
    private inheritedAttributes;
    private toggleTrack?;
    private didLoad;
    el: HTMLIonToggleElement;
    activated: boolean;
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
     * If `true`, the toggle is selected.
     */
    checked: boolean;
    /**
     * If `true`, the user cannot interact with the toggle.
     */
    disabled: boolean;
    /**
     * The value of the toggle does not mean if it's checked or not, use the `checked`
     * property for that.
     *
     * The value of a toggle is analogous to the value of a `<input type="checkbox">`,
     * it's only used when the toggle participates in a native `<form>`.
     */
    value?: string | null;
    /**
     * Enables the on/off accessibility switch labels within the toggle.
     */
    enableOnOffLabels: boolean | undefined;
    /**
     * Where to place the label relative to the input.
     * `"start"`: The label will appear to the left of the toggle in LTR and to the right in RTL.
     * `"end"`: The label will appear to the right of the toggle in LTR and to the left in RTL.
     * `"fixed"`: The label has the same behavior as `"start"` except it also has a fixed width. Long text will be truncated with ellipses ("...").
     * `"stacked"`: The label will appear above the toggle regardless of the direction. The alignment of the label can be controlled with the `alignment` property.
     */
    labelPlacement: 'start' | 'end' | 'fixed' | 'stacked';
    /**
     * How to pack the label and toggle within a line.
     * `"start"`: The label and toggle will appear on the left in LTR and
     * on the right in RTL.
     * `"end"`: The label and toggle will appear on the right in LTR and
     * on the left in RTL.
     * `"space-between"`: The label and toggle will appear on opposite
     * ends of the line with space between the two elements.
     * Setting this property will change the toggle `display` to `block`.
     */
    justify?: 'start' | 'end' | 'space-between';
    /**
     * How to control the alignment of the toggle and label on the cross axis.
     * `"start"`: The label and control will appear on the left of the cross axis in LTR, and on the right side in RTL.
     * `"center"`: The label and control will appear at the center of the cross axis in both LTR and RTL.
     * Setting this property will change the toggle `display` to `block`.
     */
    alignment?: 'start' | 'center';
    /**
     * Emitted when the user switches the toggle on or off.
     *
     * This event will not emit when programmatically setting the `checked` property.
     */
    ionChange: EventEmitter<ToggleChangeEventDetail>;
    /**
     * Emitted when the toggle has focus.
     */
    ionFocus: EventEmitter<void>;
    /**
     * Emitted when the toggle loses focus.
     */
    ionBlur: EventEmitter<void>;
    disabledChanged(): void;
    private toggleChecked;
    connectedCallback(): Promise<void>;
    componentDidLoad(): void;
    private setupGesture;
    disconnectedCallback(): void;
    componentWillLoad(): void;
    private onStart;
    private onMove;
    private onEnd;
    private getValue;
    private setFocus;
    private onClick;
    private onFocus;
    private onBlur;
    private getSwitchLabelIcon;
    private renderOnOffSwitchLabels;
    private renderToggleControl;
    private get hasLabel();
    render(): any;
}
