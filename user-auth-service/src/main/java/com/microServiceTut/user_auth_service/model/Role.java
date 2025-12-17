package com.microServiceTut.user_auth_service.model;

/**
 * User roles for authorization:
 * - USER: Regular customer (can manage cart, place orders)
 * - ADMIN: Restaurant/menu management (full CRUD on restaurants/menus)
 * - RIDER: Delivery personnel (access to delivery endpoints)
 */
public enum Role {
    USER,
    ADMIN,
    RIDER
}
