package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

public class FareCalculatorService {

    private TicketDAO ticketDAO;

    public FareCalculatorService(TicketDAO ticketDAO){
        this.ticketDAO = ticketDAO;
    }

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }
        float duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (float) (60 * 60 * 1000); // 1 hour
        if (duration < 0.5) { // 0.5 = half and hour
            ticket.setPrice(0);
        } else {
            if (ticketDAO.countTicketByVehicleRegNumber(ticket.getVehicleRegNumber()) > 1 ){
                // Calcul fare without discount
                calculateFare(ticket, duration * Fare.CAR_RATE_PER_HOUR, duration * Fare.BIKE_RATE_PER_HOUR);
            } else {
                // Calcul fare with discount
                calculateFare(ticket, (duration * Fare.CAR_RATE_PER_HOUR) * 0.95, (duration * Fare.BIKE_RATE_PER_HOUR) * 0.95);
            }
        }
    }

    private void calculateFare(Ticket ticket, double v, double v2) {
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