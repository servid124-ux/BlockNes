package raknetlib.batch;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class Zlib {
    private Zlib() {}

    public static byte[] deflate(byte[] data, int level) {
        Deflater d = new Deflater(level);
        d.setInput(data);
        d.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length / 2 + 64);
        byte[] tmp = new byte[4096];
        while (!d.finished()) out.write(tmp, 0, d.deflate(tmp));
        d.end();
        return out.toByteArray();
    }

    public static byte[] inflate(byte[] data, int maxSize) throws DataFormatException {
        try {
            return inflate(data, maxSize, false);
        } catch (DataFormatException e) {
            return inflate(data, maxSize, true);
        }
    }

    private static byte[] inflate(byte[] data, int maxSize, boolean nowrap) throws DataFormatException {
        Inflater inf = new Inflater(nowrap);
        try {
            inf.setInput(data);
            ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
            byte[] tmp = new byte[8192];
            while (!inf.finished()) {
                int n = inf.inflate(tmp);
                if (n == 0 && (inf.needsInput() || inf.needsDictionary())) throw new DataFormatException("zlib truncado");
                out.write(tmp, 0, n);
                if (out.size() > maxSize) throw new DataFormatException("zlib demasiado grande");
            }
            return out.toByteArray();
        } finally {
            inf.end();
        }
    }
}
