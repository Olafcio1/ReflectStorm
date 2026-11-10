import org.junit.Assert;
import org.junit.Test;
import pl.olafcio.reflectstorm.ReflectStorm;

public class ConstReflectionTest {
    @Test
    public void constant_get() {
        Assert.assertEquals("secret != 0xCAFEBABE", secret, ReflectStorm.builder()
                                                                                .klass(ConstReflectionTest.class)
                                                                                .field("secret", int.class)
                                                                                .get(null));
    }

    @Test
    public void constant_set() {
        System.out.println("CAFEBABE is: 0x" + Integer.toHexString(secret).toUpperCase());

        ReflectStorm.builder()
                    .klass(ConstReflectionTest.class)
                    .field("secret", int.class)
                    .set(null, 0xBBC);

        int val = (Integer) ReflectStorm.builder()
                                        .klass(ConstReflectionTest.class)
                                        .field("secret", int.class)
                                        .get(null);

        System.out.println("CAFEBABE is: 0x" + Integer.toHexString(val).toUpperCase());

        Assert.assertEquals("secret != 0xBBC", 0xBBC, val);
    }

    private static final int secret = 0xCAFEBABE;
}
