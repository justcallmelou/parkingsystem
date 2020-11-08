package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }
        calculateDuration(ticket);
    }

    private void calculateDuration(Ticket ticket) {
        float duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (float) (60 * 60 * 1000); // 1 hour
        if (duration < 0.5) { // 0.5 = half and hour
            ticket.setPrice(0);
        } else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        }
    }

    /* private void calculDiscountForRecurringUsers(Ticket ticket){
        float duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (float) (60 * 60 * 1000);
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR - (Fare.CAR_RATE_PER_HOUR * 5/100));
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR - Fare.BIKE_RATE_PER_HOUR * (5/100));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

    } */
}