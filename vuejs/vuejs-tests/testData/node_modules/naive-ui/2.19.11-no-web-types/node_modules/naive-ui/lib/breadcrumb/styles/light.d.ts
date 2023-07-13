import type { ThemeCommonVars } from '../../_styles/common';
import type { Theme } from '../../_mixins';
export declare const self: (vars: ThemeCommonVars) => {
    fontSize: string;
    itemTextColor: string;
    itemTextColorHover: string;
    itemTextColorPressed: string;
    itemTextColorActive: string;
    separatorColor: string;
    fontWeightActive: string;
};
export declare type BreadcrumbThemeVars = ReturnType<typeof self>;
declare const breadcrumbLight: Theme<'Breadcrumb', BreadcrumbThemeVars>;
export default breadcrumbLight;
export declare type BreadcrumbTheme = typeof breadcrumbLight;
