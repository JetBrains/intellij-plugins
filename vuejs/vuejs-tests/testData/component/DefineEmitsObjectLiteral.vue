<script setup lang="ts">
const emit = defineEmits({
    add(ctx: number) {
        console.log(ctx);
        return true;
    },
    change(ctx: string) {
        console.log(ctx);
        return false;
    }
});
</script>
