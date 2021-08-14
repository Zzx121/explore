package cn.edu.djtu.excel.util.basic;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * @author zzx
 * @date 2021/8/14
 */
public class CacheRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] cachedBody;

    public CacheRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        ServletInputStream is = request.getInputStream();
        cachedBody = IOUtils.toByteArray(is);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CacheServletInputStream();
    }

    @Override
    public BufferedReader getReader() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    private class CacheServletInputStream extends ServletInputStream {
        private final InputStream inputStream;

        public CacheServletInputStream() {
            inputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            try {
                return inputStream.available() == 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {

        }
    }
}
