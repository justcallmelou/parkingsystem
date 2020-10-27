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
        float duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (float) (60 * 60 * 1000);
        System.out.println("Fare Calculate : " + duration);
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
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
    }

    /* private void calculateDuration2(Ticket ticket) {
        float duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (float) (60 * 60 * 1000);
        System.out.println("Fare Calculate : " + duration);
        if (duration <= 1800000 ) {
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_FREE_THIRTY_MINUTES);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_FREE_THIRTY_MINUTES);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }

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
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    } */

}