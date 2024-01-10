import * as i0 from '@angular/core';
import * as i2 from '@angular/material/core';

declare namespace i1 {
    export {
        MatLegacyCardContent,
        MatLegacyCardTitle,
        MatLegacyCardSubtitle,
        MatLegacyCardActions,
        MatLegacyCardFooter,
        MatLegacyCardImage,
        MatLegacyCardSmImage,
        MatLegacyCardMdImage,
        MatLegacyCardLgImage,
        MatLegacyCardXlImage,
        MatLegacyCardAvatar,
        MatLegacyCard,
        MatLegacyCardHeader,
        MatLegacyCardTitleGroup
    }
}

/**
 * A basic content container component that adds the styles of a Material design card.
 *
 * While this component can be used alone, it also provides a number
 * of preset styles for common card sections, including:
 * - mat-card-title
 * - mat-card-subtitle
 * - mat-card-content
 * - mat-card-actions
 * - mat-card-footer
 *
 * @deprecated Use `MatCard` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCard {
    _animationMode?: string | undefined;
    constructor(_animationMode?: string | undefined);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCard, [{ optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyCard, "mat-card", ["matCard"], {}, {}, never, ["*", "mat-card-footer"], false, never>;
}

/**
 * Action section of a card, needed as it's used as a selector in the API.
 * @docs-private
 * @deprecated Use `MatCardActions` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardActions {
    /** Position of the actions inside the card. */
    align: 'start' | 'end';
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardActions, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardActions, "mat-card-actions", ["matCardActions"], { "align": { "alias": "align"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * Avatar image used in a card, needed to add the mat- CSS styling.
 * @docs-private
 * @deprecated Use `MatCardAvatar` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardAvatar {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardAvatar, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardAvatar, "[mat-card-avatar], [matCardAvatar]", never, {}, {}, never, never, false, never>;
}

/**
 * Content of a card, needed as it's used as a selector in the API.
 * @docs-private
 * @deprecated Use `MatCardContent` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardContent {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardContent, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardContent, "mat-card-content, [mat-card-content], [matCardContent]", never, {}, {}, never, never, false, never>;
}

/**
 * Footer of a card, needed as it's used as a selector in the API.
 * @docs-private
 * @deprecated Use `MatCardFooter` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardFooter {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardFooter, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardFooter, "mat-card-footer", never, {}, {}, never, never, false, never>;
}

/**
 * Component intended to be used within the `<mat-card>` component. It adds styles for a
 * preset header section (i.e. a title, subtitle, and avatar layout).
 * @docs-private
 * @deprecated Use `MatCardHeader` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardHeader {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardHeader, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyCardHeader, "mat-card-header", never, {}, {}, never, ["[mat-card-avatar], [matCardAvatar]", "mat-card-title, mat-card-subtitle,\n      [mat-card-title], [mat-card-subtitle],\n      [matCardTitle], [matCardSubtitle]", "*"], false, never>;
}

/**
 * Image used in a card, needed to add the mat- CSS styling.
 * @docs-private
 * @deprecated Use `MatCardImage` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardImage {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardImage, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardImage, "[mat-card-image], [matCardImage]", never, {}, {}, never, never, false, never>;
}

/**
 * Image used in a card, needed to add the mat- CSS styling.
 * @docs-private
 * @deprecated Use `MatCardLgImage` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardLgImage {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardLgImage, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardLgImage, "[mat-card-lg-image], [matCardImageLarge]", never, {}, {}, never, never, false, never>;
}

/**
 * Image used in a card, needed to add the mat- CSS styling.
 * @docs-private
 * @deprecated Use `MatCardMdImage` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardMdImage {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardMdImage, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardMdImage, "[mat-card-md-image], [matCardImageMedium]", never, {}, {}, never, never, false, never>;
}

/**
 * @deprecated Use `MatCardModule` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyCardModule, [typeof i1.MatLegacyCard, typeof i1.MatLegacyCardHeader, typeof i1.MatLegacyCardTitleGroup, typeof i1.MatLegacyCardContent, typeof i1.MatLegacyCardTitle, typeof i1.MatLegacyCardSubtitle, typeof i1.MatLegacyCardActions, typeof i1.MatLegacyCardFooter, typeof i1.MatLegacyCardSmImage, typeof i1.MatLegacyCardMdImage, typeof i1.MatLegacyCardLgImage, typeof i1.MatLegacyCardImage, typeof i1.MatLegacyCardXlImage, typeof i1.MatLegacyCardAvatar], [typeof i2.MatCommonModule], [typeof i1.MatLegacyCard, typeof i1.MatLegacyCardHeader, typeof i1.MatLegacyCardTitleGroup, typeof i1.MatLegacyCardContent, typeof i1.MatLegacyCardTitle, typeof i1.MatLegacyCardSubtitle, typeof i1.MatLegacyCardActions, typeof i1.MatLegacyCardFooter, typeof i1.MatLegacyCardSmImage, typeof i1.MatLegacyCardMdImage, typeof i1.MatLegacyCardLgImage, typeof i1.MatLegacyCardImage, typeof i1.MatLegacyCardXlImage, typeof i1.MatLegacyCardAvatar, typeof i2.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyCardModule>;
}

/**
 * Image used in a card, needed to add the mat- CSS styling.
 * @docs-private
 * @deprecated Use `MatCardSmImage` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardSmImage {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardSmImage, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardSmImage, "[mat-card-sm-image], [matCardImageSmall]", never, {}, {}, never, never, false, never>;
}

/**
 * Sub-title of a card, needed as it's used as a selector in the API.
 * @docs-private
 * @deprecated Use `MatCardSubtitle` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardSubtitle {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardSubtitle, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardSubtitle, "mat-card-subtitle, [mat-card-subtitle], [matCardSubtitle]", never, {}, {}, never, never, false, never>;
}

/**
 * Title of a card, needed as it's used as a selector in the API.
 * @docs-private
 * @deprecated Use `MatCardTitle` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardTitle {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardTitle, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardTitle, "mat-card-title, [mat-card-title], [matCardTitle]", never, {}, {}, never, never, false, never>;
}

/**
 * Component intended to be used within the `<mat-card>` component. It adds styles for a preset
 * layout that groups an image with a title section.
 * @docs-private
 * @deprecated Use `MatCardTitleGroup` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardTitleGroup {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardTitleGroup, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyCardTitleGroup, "mat-card-title-group", never, {}, {}, never, ["mat-card-title, mat-card-subtitle,\n      [mat-card-title], [mat-card-subtitle],\n      [matCardTitle], [matCardSubtitle]", "img", "*"], false, never>;
}

/**
 * Large image used in a card, needed to add the mat- CSS styling.
 * @docs-private
 * @deprecated Use `MatCardXlImage` from `@angular/material/card` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCardXlImage {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCardXlImage, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCardXlImage, "[mat-card-xl-image], [matCardImageXLarge]", never, {}, {}, never, never, false, never>;
}

export { }
