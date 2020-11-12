package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import static junit.framework.Assert.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;


    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertTrue(parkingSpot.isAvailable());
        parkingService.processIncomingVehicle();
        Connection con = null;
        Boolean isAvailable = null;
        String vehicleRegNumber = null;
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select AVAILABLE, VEHICLE_REG_NUMBER from parking, ticket where parking.PARKING_NUMBER = ? AND OUT_TIME IS NULL AND VEHICLE_REG_NUMBER = ?");
            ps.setInt(1, parkingSpot.getId());
            ps.setString(2, "ABCDEF");
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                isAvailable = rs.getBoolean(1);
                vehicleRegNumber = rs.getString(2);
            }
            dataBaseTestConfig.closeResultSet(rs);
            dataBaseTestConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        assertFalse(isAvailable);
        assertEquals(vehicleRegNumber, "ABCDEF");
        /*String vehicleRegNumber = null;
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select VEHICLE_REG_NUMBER from ticket where out_time IS NULL AND PARKING_NUMBER = ? AND VEHICLE_REG_NUMBER = ?");
            ps.setInt(1, parkingSpot.getId());
            ps.setString(2, "ABCDEF");
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                vehicleRegNumber = rs.getString(1);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        assertEquals(vehicleRegNumber, "ABCDEF"); */
    }

    @Test
    public void testParkingLotExit(){
        //TODO: check that the fare generated and out time are populated correctly in the database
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticket = new Ticket();
        parkingService.processExitingVehicle();
        Connection con = null;
        String vehicleRegNumber = "ABCDEF";
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select ticket.PARKING_NUMBER, ID, PRICE, IN_TIME, OUT_TIME, TYPE from ticket, parking where parking.parking_number = ticket.parking_number and VEHICLE_REG_NUMBER=? order by IN_TIME  limit 1");
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        assertEquals(0.0, (ticket.getPrice()));
    }
}
