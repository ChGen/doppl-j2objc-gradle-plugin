package co.touchlab.basicjava;

import org.junit.Assert;
import org.junit.Test;

public class BasicJavaTest {

    @Test
    public void testAddition() {
        Assert.assertEquals(4, 2+2);
    }

    @Test
    public void testGson() {
        Assert.assertNotNull(new com.google.gson.Gson());
    }

}