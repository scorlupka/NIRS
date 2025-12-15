package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "orderservices")
@IdClass(OrderServiceId.class)
public class OrderServiceLink {
    @Id
    @Column(name = "ordernumber")
    private Integer orderNumber;

    @Id
    @Column(name = "serviceid")
    private Long serviceId;

    public OrderServiceLink() {
    }

    public OrderServiceLink(Integer orderNumber, Long serviceId) {
        this.orderNumber = orderNumber;
        this.serviceId = serviceId;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
}

