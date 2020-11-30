package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;

    @Mock
    private static TicketDAO ticketDAO;
    private Ticket ticket;

    @BeforeEach
    private void setUpPerTest() {
        fareCalculatorService = new FareCalculatorService(ticketDAO);
        ticket = new Ticket();
    }

    @Test
    public void calculate_Fare_Car() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculate_Fare_Car_For_Recurring_Users() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.checkIfTicketByVehicleRegNumberExistsInDB(ticket.getVehicleRegNumber())).thenReturn(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);

        assertEquals(Math.round(1.425), Math.round(ticket.getPrice()));
    }

    @Test
    public void calculate_Fare_Bike() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }


    @Test
    public void calculate_Fare_Car_OneAndAHalfHour_ParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (90 * 60 * 1000));// 90 minutes parking time should give 1H30 parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(Math.round(1.5 * Fare.CAR_RATE_PER_HOUR), Math.round(ticket.getPrice()));
    }

    @Test
    public void calculate_Fare_Car_OneAndAHalfHour_ParkingTime_For_Recurring_User() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (90 * 60 * 1000));// 90 minutes parking time should give 1H30 parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.checkIfTicketByVehicleRegNumberExistsInDB(ticket.getVehicleRegNumber())).thenReturn(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(1.5 * (Fare.CAR_RATE_PER_HOUR * 0.95), (ticket.getPrice()));
    }

    @Test
    public void calculate_Fare_Bike_OneAndAHalfHour_ParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (90 * 60 * 1000));// 90 minutes parking time should give 1H30 parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals((1.5 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculate_Fare_Bike_For_Recurring_Users() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.checkIfTicketByVehicleRegNumberExistsInDB(ticket.getVehicleRegNumber())).thenReturn(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(Math.round(0.95), Math.round(ticket.getPrice()));
    }

    @Test
    public void calculate_Fare_Unknown_Type() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFareService(ticket));
    }

    @Test
    public void calculate_Fare_Car_With_Future_InTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFareService(ticket));
    }

    @Test
    public void calculate_Fare_Bike_With_Future_InTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFareService(ticket));
    }

    @Test
    public void calculate_Fare_Bike_With_Less_Than_OneHour_ParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculate_Fare_Bike_With_Less_Than_OneHour_ParkingTime_For_Recurring_User() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.checkIfTicketByVehicleRegNumberExistsInDB(ticket.getVehicleRegNumber())).thenReturn(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(Math.round(0.75 * (Fare.BIKE_RATE_PER_HOUR * 0.95)), Math.round(ticket.getPrice()));
    }

    @Test
    public void calculate_Fare_Car_With_Less_Than_OneHour_ParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculate_Fare_Car_With_Less_Than_OneHour_ParkingTime_For_Recurring_User() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.checkIfTicketByVehicleRegNumberExistsInDB(ticket.getVehicleRegNumber())).thenReturn(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(Math.round(0.75 * (Fare.CAR_RATE_PER_HOUR * 0.95)), Math.round(ticket.getPrice()));
    }

    @Test
    public void calculate_Fare_Bike_With_Less_Than_Thirty_Minute_ParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000));// first 30 minutes parking time free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(0, ticket.getPrice());
    }

    @Test
    public void calculate_Fare_Car_With_Less_Than_ThirtyMinute_ParkingTime() {

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000));// first 30 minutes parking time free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(0, ticket.getPrice());
    }

    @Test
    public void calculate_Fare_Car_With_More_Than_A_Day_ParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());

    }

    @Test
    public void calculate_Fare_Car_With_More_Than_A_Day_ParkingTime_For_Recurring_User() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.checkIfTicketByVehicleRegNumberExistsInDB(ticket.getVehicleRegNumber())).thenReturn(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(24 * (Fare.CAR_RATE_PER_HOUR * 0.95), ticket.getPrice());

    }

    @Test
    public void calculate_Fare_Bike_With_More_Than_A_Day_ParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals((24 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());

    }

    @Test
    public void calculate_Fare_Bike_With_More_Than_A_Day_ParkingTime_For_Recurring_User() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.checkIfTicketByVehicleRegNumberExistsInDB(ticket.getVehicleRegNumber())).thenReturn(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(24 * (Fare.BIKE_RATE_PER_HOUR * 0.95), ticket.getPrice());

    }

    @Test
    public void calculate_Fare_Car_One_Week_ParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (168 * 60 * 60 * 1000));// 168H = 1 week
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals((168 * Fare.CAR_RATE_PER_HOUR), (ticket.getPrice()));
    }

    @Test
    public void calculate_Fare_Car_One_Week_ParkingTime_For_Recurring_User() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (168 * 60 * 60 * 1000));// 168H = 1 week
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.checkIfTicketByVehicleRegNumberExistsInDB(ticket.getVehicleRegNumber())).thenReturn(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(168 * (Fare.CAR_RATE_PER_HOUR * 0.95), (ticket.getPrice()));
    }

    @Test
    public void calculate_Fare_Bike_One_Week_ParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (168 * 60 * 60 * 1000));// 168H = 1 week
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals((168 * Fare.BIKE_RATE_PER_HOUR), (ticket.getPrice()));
    }

    @Test
    public void calculate_Fare_Bike_One_Week_ParkingTime_For_Recurring_User() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (168 * 60 * 60 * 1000));// 168H = 1 week
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.checkIfTicketByVehicleRegNumberExistsInDB(ticket.getVehicleRegNumber())).thenReturn(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFareService(ticket);
        assertEquals(168 * (Fare.BIKE_RATE_PER_HOUR * 0.95), (ticket.getPrice()));
    }

}
