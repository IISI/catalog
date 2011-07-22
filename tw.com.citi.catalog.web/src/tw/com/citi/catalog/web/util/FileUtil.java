package tw.com.citi.catalog.web.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;

public class FileUtil {

    public static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static File prepareTempDirectory() {
        /*
         * Platform.getInstallLocation().getURL().toString();
         * -> file:/D:/temp/catalog/eclipse-1/
         * 
         * Platform.getInstanceLocation().getURL().toString();
         * -> file:/D:/temp/catalog/eclipse-1/workspace/
         * 
         * Platform.getLocation().toString();
         * -> D:/temp/catalog/eclipse-1/workspace
         * 
         * Platform.getUserLocation().getURL().toString();
         * -> file:/C:/Documents and Settings/liang/user/
         */
        try {
            File loc = new UrlResource(Platform.getInstallLocation().getURL()).getFile();
            File temp = new File(loc, "catalog_temp");
            logger.debug("temp path = {}", temp.getAbsolutePath());
            logger.debug("temp exists = {}", temp.exists());
            logger.debug("temp writable = {}", temp.canWrite());
            if (temp.exists() && temp.isDirectory()) {
                FileSystemUtils.deleteRecursively(temp);
            } else if (temp.exists() && temp.isFile()) {
                throw new RuntimeException("Failed to create folder, name has been taken.");
            }
            boolean result = temp.mkdir();
            logger.debug("create result = {}", result);
            return temp;
        } catch (IOException e) {
            throw new RuntimeException("Failed to get temp directory.", e);
        }
    }

    public static File getTempDirectory() {
        try {
            File loc = new UrlResource(Platform.getInstallLocation().getURL()).getFile();
            File temp = new File(loc, "catalog_temp");
            return temp;
        } catch (IOException e) {
            throw new RuntimeException("Failed to get temp directory.", e);
        }
    }

}
