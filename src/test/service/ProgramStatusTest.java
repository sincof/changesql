package test.service;

import com.sin.service.ProgramStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProgramStatusTest {
    @Test
    public void firstTest(){
        // 0 -> 1 -> 2 kill
        Assertions.assertEquals(1, ProgramStatus.getProgramStatus());
        Assertions.assertEquals(2, ProgramStatus.finishWR());
    }

    @Test
    public void secondTest(){
        // 2 -> 4 kill
        Assertions.assertEquals(4, ProgramStatus.getProgramStatus());
    }

    @Test
    public void thirdTest(){
        // 1 -> 3 killed
        Assertions.assertEquals(3, ProgramStatus.getProgramStatus());
        Assertions.assertEquals(4, ProgramStatus.finishWR());
    }
}
