package org.example.model;

import java.io.Serializable;
import java.util.Objects;

public class OrderServiceId implements Serializable {
    private Integer orderNumber;
    private Long serviceId;

    public OrderServiceId() {
    }

    public OrderServiceId(Integer orderNumber, Long serviceId) {
        this.orderNumber = orderNumber;
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderServiceId that = (OrderServiceId) o;
        return Objects.equals(orderNumber, that.orderNumber) && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, serviceId);
    }
}

