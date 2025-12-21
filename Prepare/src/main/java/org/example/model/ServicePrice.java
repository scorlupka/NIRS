package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "service_prices")
public class ServicePrice {
    @Id
    @Column(name = "serviceid", nullable = false)
    private Long serviceId;

    @Column(name = "baseprice", nullable = false)
    private Integer basePrice;

    public ServicePrice() {
    }

    public ServicePrice(Long serviceId, Integer basePrice) {
        this.serviceId = serviceId;
        this.basePrice = basePrice;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Integer basePrice) {
        this.basePrice = basePrice;
    }
}

