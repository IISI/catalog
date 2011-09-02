package tw.com.citi.catalog.web.util;

import java.io.Console;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.com.citi.commcypt.CommCypt;
import tw.com.citi.commcypt.util.BridgeEncode;

public class PasswordUtil {

    static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);

    public static String decodePwd(String cipher) throws Exception {
        StringBuffer plain = new StringBuffer();
        if (cipher == null || "".equals(cipher.trim())) {
            throw new IllegalArgumentException("Parameter missing.");
        }
        StringTokenizer st = new StringTokenizer(cipher, ";");
        int count = st.countTokens();
        if (count <= 0) {
            throw new IllegalArgumentException("Parameter invalid.");
        }
        List<String> tokens = new ArrayList<String>();
        int sum = 0;
        int j = 1;
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        for (int i = count; i > 0; i--) {
            sum = Integer.parseInt(tokens.get(i - 1)) - 128 - j;
            if (sum < 0) {
                sum += 256;
            }
            char c = new Character((char) sum).charValue();

            plain = plain.append(Pattern.compile("[\\p{Alnum}\\p{Punct}]").matcher(String.valueOf(c)).matches() ? c
                    : "");
            j++;
        }
        return plain.toString();
    }

    public static String encodePwd(String plain) throws Exception {
        StringBuffer cipher = new StringBuffer();
        if (plain == null || "".equals(plain.trim())) {
            throw new IllegalArgumentException("Parameter missing.");
        }
        int[] b = new int[40];
        for (int i = 0; i < plain.length(); i++) {
            b[39 - i * 2] = plain.charAt(i);
        }
        for (int i = 0; i < b.length; i++) {
            b[i] = (b[i] == 0 ? i % 2 == 0 ? 0 : 32 : b[i]) + 128 + b.length - i;
            if (b[i] >= 256) {
                b[i] -= 256;
            }
            cipher.append(String.valueOf(b[i])).append(i == b.length - 1 ? "" : ";");
        }
        return cipher.toString();
    }

    public static String encodePwdToBinaryString(String plain) throws Exception {
        StringBuffer cipher = new StringBuffer();
        if (plain == null || "".equals(plain.trim())) {
            throw new IllegalArgumentException("Parameter missing.");
        }
        int[] b = new int[40];
        for (int i = 0; i < plain.length(); i++) {
            b[39 - i * 2] = plain.charAt(i);
        }
        for (int i = 0; i < b.length; i++) {
            b[i] = (b[i] == 0 ? i % 2 == 0 ? 0 : 32 : b[i]) + 128 + b.length - i;
            if (b[i] >= 256) {
                b[i] -= 256;
            }
            String binary = Integer.toHexString(b[i]);
            cipher.append(binary.length() == 1 ? "0" + binary : binary);
        }
        return cipher.toString();
    }

    public static void main(String[] args) throws Exception {
        if (args != null && args.length > 0) {
            logger.debug("input : " + args[0]);
            logger.debug("output: " + encodePwd(args[0]));
            logger.debug("output: 0x" + encodePwdToBinaryString(args[0]));
            logger.debug("output: " + decodePwd(encodePwd(args[0])));
        }
        Console console = System.console();
        char[] password1 = console.readPassword("\nPlease input first half functional password: ");
        char[] password2 = console.readPassword("\nPlease input last half functional password: ");
        String password = new String(password1) + new String(password2);
        Arrays.fill(password1, ' ');
        Arrays.fill(password2, ' ');
        logger.debug("\nEncrypted Password :\n" + encodePwd(password));
    }
    
    /**
     * dual control password encypt
     * @param strSrc : dual control input pass
     * @return
     */
    public static String encyptDualLoginPass(String strSrc){
		
		try {
			CommCypt cypt=new CommCypt();
			return cypt.run(BridgeEncode.encode(strSrc));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return strSrc;
		}
	}
    
}
