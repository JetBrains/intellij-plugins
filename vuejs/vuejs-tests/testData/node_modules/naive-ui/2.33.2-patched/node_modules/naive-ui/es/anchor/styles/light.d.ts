import type { ThemeCommonVars } from '../../_styles/common';
import type { Theme } from '../../_mixins';
export declare const self: (vars: ThemeCommonVars) => {
    borderRadius: string;
    railColor: string;
    railColorActive: string;
    linkColor: string;
    linkTextColor: string;
    linkTextColorHover: string;
    linkTextColorPressed: string;
    linkTextColorActive: string;
    linkFontSize: string;
    linkPadding: string;
    railWidth: string;
};
export declare type AnchorThemeVars = ReturnType<typeof self>;
declare const anchorLight: Theme<'Anchor', AnchorThemeVars>;
export default anchorLight;
export declare type AnchorTheme = typeof anchorLight;
