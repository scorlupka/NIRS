package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "prices")
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "priceid")
    private Long id;

    @Column(name = "objecttype", nullable = false)
    private String objectType;

    @Column(name = "objectnumber", nullable = false)
    private Integer objectNumber;

    @Column(name = "baseprice", nullable = false)
    private Integer basePrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Integer getObjectNumber() {
        return objectNumber;
    }

    public void setObjectNumber(Integer objectNumber) {
        this.objectNumber = objectNumber;
    }

    public Integer getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Integer basePrice) {
        this.basePrice = basePrice;
    }
}

