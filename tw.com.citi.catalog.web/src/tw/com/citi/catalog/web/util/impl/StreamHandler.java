package tw.com.citi.catalog.web.util.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamHandler extends Thread {
    private static Logger logger = LoggerFactory.getLogger(StreamHandler.class);
    private InputStream is;
    private String type;
    private Set<String> files;
    private String result;
    private boolean keepResult = true;

    public StreamHandler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public StreamHandler(InputStream is, String type, boolean keepResult) {
        this.is = is;
        this.type = type;
        this.keepResult = keepResult;
    }

    public StreamHandler(InputStream is, String type, Set<String> files) {
        this.is = is;
        this.type = type;
        this.files = files;
    }

    @Override
    public void run() {
        if (keepResult) {
            result = "";
            BufferedReader br;
            try {
                br = new BufferedReader(new InputStreamReader(is, "MS950"));
                String line = "";
                String file = "";
                while ((line = br.readLine()) != null) {
                    logger.debug(type + ": " + line);
                    if (files != null) {
                        if (line.indexOf(':') >= 0) {
                            StringTokenizer st = new StringTokenizer(line, ":");
                            st.nextToken();
                            file = st.nextToken().trim();
                        } else {
                            file = line;
                        }
                        logger.debug("file: " + file);
                        if (files.contains(file)) {
                            result += line + "\n";
                        }
                    } else {
                        result += line + "\n";
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getResult() {
        return result;
    }
}
