import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { Color } from '../../interface';
import type { RangeChangeEventDetail, RangeKnobMoveEndEventDetail, RangeKnobMoveStartEventDetail, RangeValue, PinFormatter } from './range-interface';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 *
 * @slot label - The label text to associate with the range. Use the "labelPlacement" property to control where the label is placed relative to the range.
 * @slot start - Content is placed to the left of the range slider in LTR, and to the right in RTL.
 * @slot end - Content is placed to the right of the range slider in LTR, and to the left in RTL.
 *
 * @part tick - An inactive tick mark.
 * @part tick-active - An active tick mark.
 * @part pin - The counter that appears above a knob.
 * @part knob - The handle that is used to drag the range.
 * @part bar - The inactive part of the bar.
 * @part bar-active - The active part of the bar.
 * @part label - The label text describing the range.
 */
export declare class Range implements ComponentInterface {
    private rangeId;
    private didLoad;
    private noUpdate;
    private rect;
    private hasFocus;
    private rangeSlider?;
    private gesture?;
    private inheritedAttributes;
    private contentEl;
    private initialContentScrollY;
    private originalIonInput?;
    el: HTMLIonRangeElement;
    private ratioA;
    private ratioB;
    private pressedKnob;
    /**
     * The color to use from your application's color palette.
     * Default options are: `"primary"`, `"secondary"`, `"tertiary"`, `"success"`, `"warning"`, `"danger"`, `"light"`, `"medium"`, and `"dark"`.
     * For more information on colors, see [theming](/docs/theming/basics).
     */
    color?: Color;
    /**
     * How long, in milliseconds, to wait to trigger the
     * `ionInput` event after each change in the range value.
     */
    debounce?: number;
    protected debounceChanged(): void;
    /**
     * The name of the control, which is submitted with the form data.
     */
    name: string;
    /**
     * The text to display as the control's label. Use this over the `label` slot if
     * you only need plain text. The `label` property will take priority over the
     * `label` slot if both are used.
     */
    label?: string;
    /**
     * Show two knobs.
     */
    dualKnobs: boolean;
    /**
     * Minimum integer value of the range.
     */
    min: number;
    protected minChanged(): void;
    /**
     * Maximum integer value of the range.
     */
    max: number;
    protected maxChanged(): void;
    /**
     * If `true`, a pin with integer value is shown when the knob
     * is pressed.
     */
    pin: boolean;
    /**
     * A callback used to format the pin text.
     * By default the pin text is set to `Math.round(value)`.
     *
     * See https://ionicframework.com/docs/troubleshooting/runtime#accessing-this
     * if you need to access `this` from within the callback.
     */
    pinFormatter: PinFormatter;
    /**
     * If `true`, the knob snaps to tick marks evenly spaced based
     * on the step property value.
     */
    snaps: boolean;
    /**
     * Specifies the value granularity.
     */
    step: number;
    /**
     * If `true`, tick marks are displayed based on the step value.
     * Only applies when `snaps` is `true`.
     */
    ticks: boolean;
    /**
     * The start position of the range active bar. This feature is only available with a single knob (dualKnobs="false").
     * Valid values are greater than or equal to the min value and less than or equal to the max value.
     */
    activeBarStart?: number;
    protected activeBarStartChanged(): void;
    /**
     * If `true`, the user cannot interact with the range.
     */
    disabled: boolean;
    protected disabledChanged(): void;
    /**
     * the value of the range.
     */
    value: RangeValue;
    protected valueChanged(): void;
    private clampBounds;
    private ensureValueInBounds;
    /**
     * Where to place the label relative to the range.
     * `"start"`: The label will appear to the left of the range in LTR and to the right in RTL.
     * `"end"`: The label will appear to the right of the range in LTR and to the left in RTL.
     * `"fixed"`: The label has the same behavior as `"start"` except it also has a fixed width. Long text will be truncated with ellipses ("...").
     * `"stacked"`: The label will appear above the range regardless of the direction.
     */
    labelPlacement: 'start' | 'end' | 'fixed' | 'stacked';
    /**
     * The `ionChange` event is fired for `<ion-range>` elements when the user
     * modifies the element's value:
     * - When the user releases the knob after dragging;
     * - When the user moves the knob with keyboard arrows
     *
     * This event will not emit when programmatically setting the `value` property.
     */
    ionChange: EventEmitter<RangeChangeEventDetail>;
    /**
     * The `ionInput` event is fired for `<ion-range>` elements when the value
     * is modified. Unlike `ionChange`, `ionInput` is fired continuously
     * while the user is dragging the knob.
     */
    ionInput: EventEmitter<RangeChangeEventDetail>;
    /**
     * Emitted when the range has focus.
     */
    ionFocus: EventEmitter<void>;
    /**
     * Emitted when the range loses focus.
     */
    ionBlur: EventEmitter<void>;
    /**
     * Emitted when the user starts moving the range knob, whether through
     * mouse drag, touch gesture, or keyboard interaction.
     */
    ionKnobMoveStart: EventEmitter<RangeKnobMoveStartEventDetail>;
    /**
     * Emitted when the user finishes moving the range knob, whether through
     * mouse drag, touch gesture, or keyboard interaction.
     */
    ionKnobMoveEnd: EventEmitter<RangeKnobMoveEndEventDetail>;
    private setupGesture;
    componentWillLoad(): void;
    componentDidLoad(): void;
    connectedCallback(): void;
    disconnectedCallback(): void;
    private handleKeyboard;
    private getValue;
    /**
     * Emits an `ionChange` event.
     *
     * This API should be called for user committed changes.
     * This API should not be used for external value changes.
     */
    private emitValueChange;
    /**
     * The value should be updated on touch end or
     * when the component is being dragged.
     * This follows the native behavior of mobile devices.
     *
     * For example: When the user lifts their finger from the
     * screen after tapping the bar or dragging the bar or knob.
     */
    private onStart;
    /**
     * The value should be updated while dragging the
     * bar or knob.
     *
     * While the user is dragging, the view
     * should not scroll. This is to prevent the user from
     * feeling disoriented while dragging.
     *
     * The user can scroll on the view if the knob or
     * bar is not being dragged.
     *
     * @param detail The details of the gesture event.
     */
    private onMove;
    /**
     * The value should be updated on touch end:
     * - When the user lifts their finger from the screen after
     * tapping the bar.
     *
     * @param detail The details of the gesture or mouse event.
     */
    private onEnd;
    private update;
    private setPressedKnob;
    private get valA();
    private get valB();
    private get ratioLower();
    private get ratioUpper();
    private updateRatio;
    private updateValue;
    private setFocus;
    private onBlur;
    private onFocus;
    /**
     * Returns true if content was passed to the "start" slot
     */
    private get hasStartSlotContent();
    /**
     * Returns true if content was passed to the "end" slot
     */
    private get hasEndSlotContent();
    private get hasLabel();
    private renderRangeSlider;
    render(): any;
}
