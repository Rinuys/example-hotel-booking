
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import PaymentManager from "./components/PaymentManager"

import RoomManager from "./components/RoomManager"


import GuestManagementPage from "./components/GuestManagementPage"
import BookManager from "./components/BookManager"

import NotificationManager from "./components/NotificationManager"

import MarketingManager from "./components/MarketingManager"

import ServiceCenterManager from "./components/ServiceCenterManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/Payment',
                name: 'PaymentManager',
                component: PaymentManager
            },

            {
                path: '/Room',
                name: 'RoomManager',
                component: RoomManager
            },


            {
                path: '/GuestManagementPage',
                name: 'GuestManagementPage',
                component: GuestManagementPage
            },
            {
                path: '/Book',
                name: 'BookManager',
                component: BookManager
            },

            {
                path: '/Notification',
                name: 'NotificationManager',
                component: NotificationManager
            },

            {
                path: '/Marketing',
                name: 'MarketingManager',
                component: MarketingManager
            },

            {
                path: '/ServiceCenter',
                name: 'ServiceCenterManager',
                component: ServiceCenterManager
            },



    ]
})
