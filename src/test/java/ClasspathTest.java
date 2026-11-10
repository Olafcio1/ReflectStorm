import org.junit.Assert;
import org.junit.Test;
import pl.olafcio.reflectstorm.Abstraction;
import pl.olafcio.reflectstorm.ReflectStorm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ClasspathTest {
    @Test
    public void addToClasspath() throws IOException {
        final String content = "expected";
        final File file = File.createTempFile("reflectstorm", "classpathFile.jar");

        file.deleteOnExit();

        final FileOutputStream fos = new FileOutputStream(file);
        final ZipOutputStream zos = new ZipOutputStream(fos);

        try {
            zos.putNextEntry(new ZipEntry("test.txt"));
            zos.write(content.getBytes("UTF8"));
        } finally {
            zos.close();
            fos.close();
        }

        ReflectStorm.classpath().klassLoader(ReflectStorm.Classpath.BuiltinCL.APP)
                                .addFile(file.getPath());

        InputStream stream = Abstraction.get().getClassLoader(ReflectStorm.Classpath.BuiltinCL.APP)
                                              .getResourceAsStream("test.txt");

        Assert.assertNotNull("stream is null", stream);

        try {
            byte[] read = new byte[content.length()];
            int offset = 0;

            while (offset < read.length) {
                int read1 = stream.read(read);
                if (read1 == -1)
                    break;

                offset += read1;
            }

            String readstr = new String(read);
            Assert.assertEquals(String.format("expected resource content '%s', got '%s'", content, readstr), content, readstr);
        } finally {
            stream.close();
        }
    }
}
