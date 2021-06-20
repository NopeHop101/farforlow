package com.main.farforlow;

import com.main.farforlow.entity.Result;
import com.main.farforlow.parser.SkiplaggedClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Calendar;
import java.util.Date;

@SpringBootTest
class FarforlowApplicationTests {

    private SkiplaggedClient skiplaggedClient;

    @Test
    void contextLoads() {
        this.skiplaggedClient = new SkiplaggedClient();
    }

    @Test
    public void skiplaggedParserTest() {
        Result result = new Result();
        Calendar c = Calendar.getInstance();
        c.set(2021, 10, 11);
        Date deptDate = c.getTime();
        c.set(2021, 10, 21);
        Date retDate = c.getTime();
        result = skiplaggedClient.getTripOptionBestPrice("GVA", "MEX", deptDate, retDate);
        System.out.println(result);
    }

}
