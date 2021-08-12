package com.main.farforlow;

import com.main.farforlow.entity.UserRequest;
import com.main.farforlow.exception.DurationException;
import com.main.farforlow.exception.RequestsQuantityException;
import com.main.farforlow.exception.SearchPeriodException;
import com.main.farforlow.service.Utils;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UtilsTest {

    @Test
    public void getDurationDatesTest() {
        Utils utils = new Utils();
        List<Integer> expectedRes = new ArrayList<>(Arrays.asList(10, 14));
        try {
            List<Integer> actualRes = utils.getDurationDays(" 10-14");
            Assert.assertEquals(actualRes, expectedRes);
        } catch (Exception e) {
        }
        try {
            List<Integer> actualRes = utils.getDurationDays(" 15-14");
            Assert.fail();
        } catch (DurationException e) {
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail();
        }
        try {
            List<Integer> actualRes = utils.getDurationDays(" 10-14-1");
            Assert.fail();
        } catch (DurationException e) {
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail();
        }
        try {
            List<Integer> actualRes = utils.getDurationDays(" AA-14");
            Assert.fail();
        } catch (DurationException e) {
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail();
        }
        try {
            List<Integer> actualRes = utils.getDurationDays(" 15.14");
            Assert.fail();
        } catch (DurationException e) {
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void getSearchPeriodDatesTest() {
        Utils utils = new Utils();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        UserRequest userRequest = new UserRequest();
        userRequest.setMinTripDurationDays(8);
        userRequest.setMaxTripDurationDays(10);
        try {
            List<Date> expectedRes = new ArrayList<>(Arrays.asList(format.parse("20.03.2030"), format.parse("20.05.2030")));
            List<Date> actualRes = utils.getSearchPeriodDates("20.03.2030-20.05.2030", userRequest);
            Assert.assertEquals(actualRes, expectedRes);

            try {
                actualRes = utils.getSearchPeriodDates("20.06.2030-20.05.2030", userRequest);
                Assert.fail();
            } catch (SearchPeriodException e) {
                Assert.assertTrue(true);
            } catch (Exception e) {
                Assert.fail();
            }

            try {
                actualRes = utils.getSearchPeriodDates("20.04.2030-25.04.2030", userRequest);
                Assert.assertTrue(true);
                Assert.fail();
            } catch (SearchPeriodException e) {
                Assert.fail();
            }

            try {
                actualRes = utils.getSearchPeriodDates("20-04.2030-20.05.2030", userRequest);
                Assert.fail();
            } catch (SearchPeriodException e) {
                Assert.assertTrue(true);
            } catch (Exception e) {
                Assert.fail();
            }

            try {
                actualRes = utils.getSearchPeriodDates("20/06/2030-20/05/2030", userRequest);
                Assert.fail();
            } catch (SearchPeriodException e) {
                Assert.assertTrue(true);
            } catch (Exception e) {
                Assert.fail();
            }

        } catch (Exception e) {
        }
    }

    @Test
    public void getRequestsQuantity() {
        Utils utils = new Utils();
        UserRequest userRequest = new UserRequest();
        userRequest.setMinTripDurationDays(14);
        userRequest.setMaxTripDurationDays(21);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            userRequest.setEarliestTripStartDate(formatter.parse("01.10.2021"));
            userRequest.setLatestReturnDate(formatter.parse("15.11.2021"));
        } catch (Exception e) {
        }
        userRequest.setDepartureAirports(Arrays.asList("GVA"));
        userRequest.setDestinationAirports(Arrays.asList("MEX"));
        Integer expectedRes = 232;
        try {
            Integer actualRes = utils.getRequestsQuantity(userRequest);
            Assert.assertEquals(actualRes, expectedRes);
        } catch (Exception e) {
        }

        try {
            userRequest.setMinTripDurationDays(1);
            userRequest.setMaxTripDurationDays(30);
            try {
                userRequest.setEarliestTripStartDate(formatter.parse("10.11.2021"));
                userRequest.setLatestReturnDate(formatter.parse("25.05.2022"));
            } catch (Exception e) {
            }
            Integer actualRes = utils.getRequestsQuantity(userRequest);
            Assert.fail();
        } catch (RequestsQuantityException e) {
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void isRequestInformationFullTest() {
        Utils utils = new Utils();
        UserRequest userRequest = new UserRequest();
        userRequest.setUserName("dusha");
        userRequest.setRequestsQuantity(60);
        userRequest.setMinTripDurationDays(10);
        userRequest.setMaxTripDurationDays(12);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            userRequest.setEarliestTripStartDate(formatter.parse("10.11.2021"));
            userRequest.setLatestReturnDate(formatter.parse("25.11.2021"));
        } catch (Exception e) {
        }
        userRequest.setDepartureAirports(Arrays.asList("DME", "SVO"));
        userRequest.setDestinationAirports(Arrays.asList("LHR", "LCY"));
        Assert.assertTrue(utils.isRequestInformationFull(userRequest));
        userRequest.setDestinationAirports(null);
        Assert.assertFalse(utils.isRequestInformationFull(userRequest));
    }
}
