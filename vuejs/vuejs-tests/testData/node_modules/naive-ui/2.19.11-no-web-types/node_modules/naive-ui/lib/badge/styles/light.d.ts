import type { ThemeCommonVars } from '../../_styles/common';
import { Theme } from '../../_mixins';
declare const self: (vars: ThemeCommonVars) => {
    color: string;
    colorInfo: string;
    colorSuccess: string;
    colorError: string;
    colorWarning: string;
    fontSize: string;
    fontFamily: string;
};
export declare type BadgeThemeVars = ReturnType<typeof self>;
declare const badgeLight: Theme<'Badge', BadgeThemeVars>;
export default badgeLight;
export declare type BadgeTheme = typeof badgeLight;
