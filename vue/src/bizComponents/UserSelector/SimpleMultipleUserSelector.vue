<template>
    <el-select :value="value" :disabled="disabled" multiple size="small" @input="emit">
        <el-option
                v-for="user in data"
                :key="user.id"
                :label="user.name"
                :value="user.id"
        />
    </el-select>
</template>

<script>
    import {getUsers} from '@/api/system/user'

    export default {
        name: "SimpleMultipleUserSelector",

        props: {
            value: Array,
            disabled: Boolean
        },

        data() {
            return {
                data: []
            }
        },

        methods: {
            emit(v) {
                this.$emit('input', v)
            }
        },

        mounted() {
            getUsers({page: 1, pageSize: 9999})
                .then(({list}) => this.data = list)
        }
    }
</script>
