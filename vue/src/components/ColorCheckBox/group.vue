<template>
    <div class="color-checkbox-group">
        <slot/>
    </div>
</template>

<script>
    export default {
        name: "ColorCheckGroup",

        provide() {
            return {
                colorCheckGroup: this
            }
        },

        props: {value: String},

        data: () => ({children: []})
    }
</script>

<style lang="scss">
    @import "~@/assets/styles/variables.scss";

    .color-checkbox-group {
        @include clearfix
    }
</style>
