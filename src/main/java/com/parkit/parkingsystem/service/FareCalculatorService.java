package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

/**
 * FareCalculatorService calculates fare according to parking type and duration
 */
public class FareCalculatorService {

    private final TicketDAO ticketDAO;

    public FareCalculatorService(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    public void calculateFareService(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }
        float duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (float) (60 * 60 * 1000); // 1 hour
        if (duration < 0.5) { // 0.5 = half and hour
            ticket.setPrice(0);
        } else {
            if (ticketDAO.countTicketByVehicleRegNumber(ticket.getVehicleRegNumber()) > 0) {
                // Calculate fare with discount
                calculateTicketFare(ticket, duration * (Fare.CAR_RATE_PER_HOUR * 0.95), duration * (Fare.BIKE_RATE_PER_HOUR * 0.95));
            } else {
                // Calculate fare without discount
                calculateTicketFare(ticket, duration * Fare.CAR_RATE_PER_HOUR, duration * Fare.BIKE_RATE_PER_HOUR);
            }
        }
    }

    /**
     * @param ticket
     * @param v      calculation ticket fare for car
     * @param v2     calculation ticket fare for bike
     */
    private void calculateTicketFare(Ticket ticket, double v, double v2) {
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(v);
                break;
            }
            case BIKE: {
                ticket.setPrice(v2);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}