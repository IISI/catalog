package tw.com.citi.catalog.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.com.citi.catalog.conf.Jcifs;

public class NetUseUtil {
    private static Logger logger = LoggerFactory.getLogger(NetUseUtil.class);

    public static String mappingLocalPath(String netPath) {
        String localPath = "";
        String result = "";
        Process process = null;
        BufferedReader bf = null;
        try {
            String command = "net use";
            String[] cmd = new String[] { "cmd", "/C", command };
            process = Runtime.getRuntime().exec(cmd);
            bf = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS950"));
            String line = "";
            String netServer = netPath.substring(0, netPath.indexOf("\\", 2));
            while ((line = bf.readLine()) != null) {
                result += line + "\n";
                // net use 顯示的資料要包含 netServer，且 netPath 要包含 net use 的遠端路徑資料中
                if (line.toLowerCase().indexOf(netServer.toLowerCase()) > 0) {
                    StringTokenizer st = new StringTokenizer(line, " ", false);
                    st.nextToken();
                    String driver = st.nextToken();
                    String path = st.nextToken();
                    int index = netPath.toLowerCase().indexOf(path.toLowerCase());
                    if (index >= 0) {
                        localPath = driver + netPath.substring(index + path.length());
                    }
                }
            }
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage() + "\n" + result, e);
        }
        return localPath;
    }

    public static int disconnectAllNetworkPath() {
        String result = "";
        Process process = null;
        BufferedReader bf = null;
        try {
            String command = "net use * /delete /y";
            String[] cmd = new String[] { "cmd", "/C", command };
            process = Runtime.getRuntime().exec(cmd);
            bf = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS950"));
            String line = "";
            while ((line = bf.readLine()) != null) {
                result += line + "\n";
            }
            bf.close();
            logger.debug(result);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage() + "\n" + result, e);
        }
        return process == null ? -1 : process.exitValue();
    }

    public static int connectNetworkDrive(String netPath) {
        netPath = netPath.trim();
        if (netPath.lastIndexOf("\\") == netPath.length() - 1) {
            netPath = netPath.substring(0, netPath.length() - 1);
        }
        String result = "";
        Process process = null;
        BufferedReader bf = null;
        try {
            StringBuffer command = new StringBuffer();
            command.append("net use ").append(getFreeDriveLetter()).append(": ").append(netPath).append(" /user:");
            if (!"".equals(Jcifs.getDomain())) {
                command.append(Jcifs.getDomain()).append("\\");
            }
            command.append(Jcifs.getUsername()).append(" ").append(Jcifs.getUserpassword());
            String[] cmd = new String[] { "cmd", "/C", command.toString() };
            process = Runtime.getRuntime().exec(cmd);
            bf = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS950"));
            String line = "";
            while ((line = bf.readLine()) != null) {
                result += line + "\n";
            }
            bf.close();
            logger.debug(result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage() + "\n" + result, e);
        }
        return process == null ? -1 : process.exitValue();
    }

    private static String getFreeDriveLetter() {
        String all = "ZYXWVUTSRQPONMLKJIHGFEDCBA";
        File[] roots = File.listRoots();
        for (File root : roots) {
            String s = root.toString().toUpperCase();
            s = s.substring(0, s.indexOf(':'));
            all = all.replace(s, "");
        }
        return String.valueOf(all.charAt(0));
    }
}
