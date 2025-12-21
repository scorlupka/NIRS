package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "room_prices")
public class RoomPrice {
    @Id
    @Column(name = "roomnumber", nullable = false)
    private Integer roomNumber;

    @Column(name = "baseprice", nullable = false)
    private Integer basePrice;

    public RoomPrice() {
    }

    public RoomPrice(Integer roomNumber, Integer basePrice) {
        this.roomNumber = roomNumber;
        this.basePrice = basePrice;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Integer basePrice) {
        this.basePrice = basePrice;
    }
}

