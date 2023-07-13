import type { ThemeCommonVars } from '../../../_styles/common';
import type { Theme } from '../../../_mixins';
export declare const self: (vars: ThemeCommonVars) => {
    color: string;
    colorHover: string;
};
export declare type ScrollbarThemeVars = ReturnType<typeof self>;
declare const scrollbarLight: Theme<'Scrollbar', ScrollbarThemeVars>;
export default scrollbarLight;
export declare type ScrollbarTheme = typeof scrollbarLight;
