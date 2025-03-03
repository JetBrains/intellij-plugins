import {Mixin} from "./mixin"


export default {
    template: "<div></div>",

    mixins: [
        Mixin.getByName('cms-element'),
    ],

    data() {
        return {
            galleryLimit: 3,
            activeMedia: null,
        };
    },

    computed: {
        currentDeviceView() {
            // this.config is not defined - no errors here
            return this.config.element.autocapitalize;
        },

        galleryPositionClass() {
            // this.element is defined - error on autocapitalize
            return `is--preview-${this.element.config.<weak_warning descr="Unresolved variable autocapitalize">autocapitalize</weak_warning>.value}`;
        },

        mediaUrls() {
            const config = this.element?.config;

            if (!config || config.<weak_warning descr="Unresolved variable source">source</weak_warning> === 'default') {
                return [];
            }

            return this.element?.data?.<weak_warning descr="Unresolved variable sliderItems">sliderItems</weak_warning> || [];
        },
    },

};
