package tw.com.citi.catalog.web.utils;

import java.util.Map;

public class AccessControlUtil {
    public static boolean authenticateCBCUser(String checkerId, String checkerPwd) {
        // TODO
        return true;
    }

    public static boolean authenticateCBCUser(Map<String, String> dataMap) {
        String checkerId = dataMap.get("checkerId");
        String checkerPwd = dataMap.get("checkerPwd");
        return authenticateCBCUser(checkerId, checkerPwd);
    }
}
