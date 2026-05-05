import { defineComponent, h } from "vue";
export default defineComponent({
    name: "MyComponent",
    props: {
        message: { type: String, required: true }
    },
    emits: {
        custom(message) {
            return true;
        }
    },
    setup(props, ctx) {
        return () => {
            return h("button", {
                onClick() {
                    ctx.emit("custom", props.message);
                }
            }, props.message);
        };
    }
});
