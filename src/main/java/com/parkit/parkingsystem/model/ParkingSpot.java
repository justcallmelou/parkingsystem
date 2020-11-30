package com.parkit.parkingsystem.model;

import com.parkit.parkingsystem.constants.ParkingType;

/**
 * Parking spot class represents a parking spot with different values : number, parking type and availability
 */
public class ParkingSpot {
    private int number;
    private ParkingType parkingType;
    private boolean isAvailable;

    /**
     * @param number      Parking spot number
     * @param parkingType Parking spot type
     * @param isAvailable Parking spot availability
     */
    public ParkingSpot(int number, ParkingType parkingType, boolean isAvailable) {
        this.number = number;
        this.parkingType = parkingType;
        this.isAvailable = isAvailable;
    }

    /**
     * @return Parking spot number
     */
    public int getId() {
        return number;
    }

    /**
     * @param number update parking spot number
     */
    public void setId(int number) {
        this.number = number;
    }

    /**
     * @return parking type (car or bike)
     */
    public ParkingType getParkingType() {
        return parkingType;
    }

    public void setParkingType(ParkingType parkingType) {
        this.parkingType = parkingType;
    }

    /**
     * @return parking spot availability
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpot that = (ParkingSpot) o;
        return number == that.number;
    }

    @Override
    public int hashCode() {
        return number;
    }

}
