package tw.com.citi.catalog.web.util.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamHandler extends Thread {
    private static Logger logger = LoggerFactory.getLogger(StreamHandler.class);
    private InputStream is;
    private String type;
    private String result;

    public StreamHandler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    @Override
    public void run() {
        result = "";
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(is, "MS950"));
            String line = "";
            while ((line = br.readLine()) != null) {
                logger.debug(type + ": " + line);
                if (line.indexOf("(PCLI)") > 0 || line.indexOf("All rights reserved.") > 0) {
                    // ignore first 2 lines
                    continue;
                }
                result += line + "\n";
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResult() {
        return result;
    }
}
