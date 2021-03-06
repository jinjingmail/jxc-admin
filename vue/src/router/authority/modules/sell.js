/*路由表：销售管理*/
import Layout from '@/layout'
import {lazyLoadView} from "@/router/util"

const router = {
    path: '/document/sell',
    component: Layout,
    alwaysShow: true,
    meta: {title: '销售管理', icon: 'sell'},
    children: [
        {
            path: 'order',
            name: 'sellOrder',
            component: lazyLoadView(import('@/views/sell/order')),
            meta: {title: '销售订单'}
        },
        {
            path: 'order/detail/:type(see|add|edit)/:id?',
            name: 'sellOrderDetail',
            props: true,
            component: lazyLoadView(import('@/views/sell/order/detail')),
            meta: {
                dynamicTitle(to) {
                    const {type, id} = to.params
                    switch (type) {
                        case 'add':
                            return '添加销售订单'
                        case 'edit':
                            return `编辑销售订单${id}`
                        case 'see':
                            return `查看销售订单${id}`
                    }
                },
                activeMenu: '/document/sell/order',
                isDetailPage: true
            }
        },
        {
            path: 'outbound',
            name: 'sellOutbound',
            component: lazyLoadView(import('@/views/sell/outbound')),
            meta: {title: '销售出库'}
        },
        {
            path: 'outbound/detail/:type(see|add|edit)/:id?',
            name: 'purchaseOutboundDetail',
            props: true,
            component: lazyLoadView(import('@/views/sell/outbound/detail')),
            meta: {
                dynamicTitle(to) {
                    const {type, id} = to.params
                    switch (type) {
                        case 'add':
                            return '添加销售出库单'
                        case 'edit':
                            return `编辑销售出库单${id}`
                        case 'see':
                            return `查看销售出库单${id}`
                    }
                },
                activeMenu: '/document/outbound/order',
                isDetailPage: true
            }
        }
    ]
}

export default router
