import org.example.Helper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mock;

public class StressTests {
    @Mock
    Helper helper;

    @BeforeEach
    public void initialize() {
        helper = new Helper();
    }

    @RepeatedTest(50000)
    public void stressTest_shouldAlways_EndProgram_gracefully() {
        helper.threadCreation();
        helper.eventCreation();
        Assertions.assertEquals( 26, helper.getResultInt());
    }
}
