package com.playdata.orderingservice.ordering.entity;

public enum OrderStatus {
    ORDERED,
    PENDING_USER_FAILURE,
    PENDING_PROD_NOT_FOUND,
    PENDING_PROD_STOCK_UPDATE,
    CANCELED
}
