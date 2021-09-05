package cn.edu.djtu.excel.util.basic;

import com.google.common.io.ByteStreams;
import org.springframework.http.MediaType;

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
        String contentType = getContentType();
        if (contentType != null && contentType.equalsIgnoreCase(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            //handle application/x-www-form-urlencoded
            //// init cache in ContentCachingRequestWrapper.  THIS IS THE VITAL CALL so that "@RequestParam Map<String, String> parameters" are populated on the REST Controller.
            // See https://stackoverflow.com/questions/10210645/http-servlet-request-lose-params-from-post-body-after-read-it-once/64924380#64924380
            super.getParameterMap();
            cachedBody = new byte[0];
        } else if (contentType != null && contentType.equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {
            //handle body json
            cachedBody = ByteStreams.toByteArray(request.getInputStream());
        } else {
            cachedBody = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CacheServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
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
