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
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select p.AVAILABLE from parking p where p.PARKING_NUMBER = ?");
            ps.setInt(1, parkingSpot.getId());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                isAvailable = rs.getBoolean(1);;
            }
            dataBaseTestConfig.closeResultSet(rs);
            dataBaseTestConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        assertFalse(isAvailable);

        String vehicleRegNumber = null;
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
        assertEquals(vehicleRegNumber, "ABCDEF");
    }

    @Test
    public void testParkingLotExit(){
        //TODO: check that the fare generated and out time are populated correctly in the database
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticket = new Ticket();
        parkingService.processExitingVehicle();
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select VEHICLE_REG_NUMBER from ticket where out_time IS NOT NULL AND update ticket set PRICE=?, OUT_TIME=?, ID=? ");
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3, ticket.getId());
            ps.execute();
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket.setPrice(rs.getDouble(3));
                ticket.setOutTime(rs.getTimestamp(5));

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        assertEquals(0.00, ticket.getPrice());

    }

}

