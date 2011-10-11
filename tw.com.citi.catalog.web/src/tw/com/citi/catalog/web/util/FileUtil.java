package tw.com.citi.catalog.web.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

public class FileUtil {

    public static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static File prepareTempDirectory() {
        FileUtil.deleteTempDirectory();
        File temp = FileUtil.getTempDirectory();
        boolean result = temp.mkdir();
        logger.debug("create result = {}", result);
        return temp;
    }

    public static File getTempDirectory() {
        File temp = new File("C:\\Temp\\jcs");
        return temp;
    }

    public static void deleteTempDirectory() {
        File temp = FileUtil.getTempDirectory();
        logger.debug("temp path = {}", temp.getAbsolutePath());
        logger.debug("temp exists = {}", temp.exists());
        logger.debug("temp writable = {}", temp.canWrite());
        if (temp.exists() && temp.isDirectory()) {
            FileSystemUtils.deleteRecursively(temp);
        } else if (temp.exists() && temp.isFile()) {
            throw new RuntimeException("Failed to create folder, name has been taken.");
        }
    }

}
