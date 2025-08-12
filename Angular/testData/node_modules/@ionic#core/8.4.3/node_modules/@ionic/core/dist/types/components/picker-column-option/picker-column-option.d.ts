import type { ComponentInterface } from '../../stencil-public-runtime';
import type { Color } from '../../interface';
export declare class PickerColumnOption implements ComponentInterface {
    /**
     * We keep track of the parent picker column
     * so we can update the value of it when
     * clicking an enable option.
     */
    private pickerColumn;
    el: HTMLElement;
    /**
     * The aria-label of the option.
     *
     * If the value changes, then it will trigger a
     * re-render of the picker since it's a @State variable.
     * Otherwise, the `aria-label` attribute cannot be updated
     * after the component is loaded.
     */
    ariaLabel?: string | null;
    /**
     * If `true`, the user cannot interact with the picker column option.
     */
    disabled: boolean;
    /**
     * The text value of the option.
     */
    value?: any | null;
    /**
     * The color to use from your application's color palette.
     * Default options are: `"primary"`, `"secondary"`, `"tertiary"`, `"success"`, `"warning"`, `"danger"`, `"light"`, `"medium"`, and `"dark"`.
     * For more information on colors, see [theming](/docs/theming/basics).
     */
    color?: Color;
    /**
     * The aria-label of the option has changed after the
     * first render and needs to be updated within the component.
     *
     * @param ariaLbl The new aria-label value.
     */
    onAriaLabelChange(ariaLbl: string): void;
    componentWillLoad(): void;
    connectedCallback(): void;
    disconnectedCallback(): void;
    /**
     * The column options can load at any time
     * so the options needs to tell the
     * parent picker column when it is loaded
     * so the picker column can ensure it is
     * centered in the view.
     *
     * We intentionally run this for every
     * option. If we only ran this from
     * the selected option then if the newly
     * loaded options were not selected then
     * scrollActiveItemIntoView would not be called.
     */
    componentDidLoad(): void;
    /**
     * When an option is clicked, update the
     * parent picker column value. This
     * component will handle centering the option
     * in the column view.
     */
    onClick(): void;
    render(): any;
}
