<template>
    <el-card v-loading="loading" class="card-container max-view-height">
        <div class="tip-row">
            <a href="https://admin.iviewui.com/components/org_tree_page" target="_blank">
                组织结构树演示，详情见iview-admin
            </a>
        </div>

        <div class="department-outer">
            <div class="view-box">
                <org-tree-view
                        :data="data"
                        :horizontal="horizontal"
                        :zoom="realZoom"
                        collapsable
                        @node-contextmenu="openMenu"
                        @wheel.native.prevent="wheel"
                />
            </div>

            <div class="tooltip-box">
                <el-radio-group v-model="horizontal" fill="#909399" size="small" style="margin-right: 20px">
                    <el-radio-button :label="true">横 向</el-radio-button>
                    <el-radio-button :label="false">垂 直</el-radio-button>
                </el-radio-group>

                <el-button
                        circle
                        icon="el-icon-refresh"
                        size="small"
                        style="margin-right: 20px"
                        title="刷新"
                        type="info"
                        @click="search"
                />

                <zoom-control v-model="zoom" :max="200" :min="20"/>
            </div>
        </div>

        <context-menu
                v-if="canAdd||canUpdate||canDel"
                v-model="contextmenu.show"
                :left="contextmenu.left"
                :top="contextmenu.top"
        >
            <context-menu-item v-show="canAdd" @click="add">添加部门</context-menu-item>
            <context-menu-item v-show="canUpdate" @click="update">编辑部门</context-menu-item>
            <context-menu-item v-show="canDel" @click="del">删除部门</context-menu-item>
        </context-menu>

        <edit-dialog v-model="editDialog" :data="currentNode" :type="type" @success="search"/>
    </el-card>
</template>

<script>
    import OrgTreeView from "./components/OrgTreeView"
    import ZoomControl from '@/components/ZoomControl'
    import ContextMenu from "@/components/ContextMenu"
    import ContextMenuItem from "@/components/ContextMenu/ContextMenuItem"
    import EditDialog from "./components/EditDialog"
    import {auth} from "@/utils/auth"
    import {createTreeByWorker} from "@/utils/tree"
    import {isEmpty} from '@/utils'
    import {elConfirm, elError, elSuccess} from "@/utils/message"
    import {delDepartment, getAllDepartments} from "@/api/system/department"

    const baseUrl = '/system/department'

    export default {
        name: "departmentManagement",

        components: {OrgTreeView, ZoomControl, ContextMenu, ContextMenuItem, EditDialog},

        data() {
            return {
                loading: false,
                data: {},
                zoom: 100,
                horizontal: false,
                currentNode: null,
                type: 'add',
                editDialog: false,
                contextmenu: {
                    show: false,
                    left: 0,
                    top: 0
                }
            }
        },

        computed: {
            realZoom() {
                return this.zoom / 100
            },

            canAdd() {
                return auth(baseUrl + '/add')
            },

            canUpdate() {
                return auth(baseUrl + '/update')
            },

            canDel() {
                return auth(baseUrl + '/del')
            },
        },

        methods: {
            openMenu(e, obj) {
                e.preventDefault()
                this.currentNode = obj
                const {left, top} = this.$el.getBoundingClientRect()
                this.contextmenu.left = e.clientX - left + 15
                this.contextmenu.top = e.clientY - top
                this.contextmenu.show = true
            },

            wheel(e) {
                const eventDelta = e.wheelDelta || -(e.detail || 0)
                const zoom = this.zoom + eventDelta / 12 * 2
                if (zoom >= 20 && zoom <= 200) {
                    this.zoom = zoom
                }
            },

            add() {
                this.type = 'add'
                this.editDialog = true
            },

            update() {
                this.type = 'edit'
                this.editDialog = true
            },

            del() {
                if (this.loading || !this.currentNode) return
                if (this.currentNode.id === 1) return elError('根节点不能删除')
                if (this.currentNode.children && this.currentNode.children.length > 0) {
                    return elError('请先删除子部门')
                }
                elConfirm(`确定删除部门【${this.currentNode.name}】？`)
                    .then(() => {
                        this.loading = true
                        return delDepartment(this.currentNode)
                    })
                    .then(({msg}) => {
                        elSuccess(msg)
                        this.search()
                    })
                    .finally(() => this.loading = false)
            },

            search() {
                this.currentNode = null
                this.loading = true
                getAllDepartments()
                    .then(data => createTreeByWorker(data))
                    .then(data => {
                        completeDepartment(data)
                        this.data = data[0]
                    })
                    .finally(() => this.loading = false)
            }
        },

        mounted() {
            this.search()
        }
    }

    //将部门树转换为页面需要的结构
    function completeDepartment(arr, name) {
        arr.forEach(i => {
            i.label = i.name
            i.parentName = name

            if (isEmpty(name)) i.fullname = i.name
            else i.fullname = name + ' > ' + i.name

            if (i.children) completeDepartment(i.children, i.fullname)
        })
    }
</script>

<style lang="scss">
    .department-outer {
        width: 100%;
        height: 100%;
        overflow: hidden;

        .tooltip-box {
            position: absolute;
            right: 30px;
            bottom: 30px;
            z-index: 2;
            display: flex;
            justify-content: flex-end;
            align-items: center;
        }

        .view-box {
            position: absolute;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
        }
    }
</style>
