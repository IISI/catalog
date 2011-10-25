package tw.com.citi.catalog.web.util.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import tw.com.citi.catalog.web.util.FileUtil;
import tw.com.citi.catalog.web.util.IPvcsCmd;

public class PvcsCmd implements IPvcsCmd {

    private static Logger logger = LoggerFactory.getLogger(PvcsCmd.class);

    /**
     * 查詢受到 PVCS 管控的檔案列表。
     * 
     * pcli ListVersionedFiles -pr"${projectDatabase}"
     * -id"${username}:${password}" -pp"${projectPath}" -l -z ${path}
     * 
     * @param projectDatabase
     * @param projectPath
     * @param username
     * @param password
     * @param path
     * @return
     */
    @Override
    public String[] listVersionedFiles(String projectDatabase, String projectPath, String username, String password,
            String path) {
        // TODO Auto-generated method stub
        ArrayList<String> versionedFilesArrayList = new ArrayList<String>();
        try {

            String command = "pcli ListVersionedFiles -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password
                    + "\" ";

            if (projectPath != null && !projectPath.trim().equals("")) {
                command += "-pp\"" + projectPath + "\" -l -z";

                if (path != null && !path.trim().equals("")) {
                    command += " " + path;
                }

            }
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String file = "";
            while ((file = bf.readLine()) != null) {
                versionedFilesArrayList.add(file);
            }
            bf.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String[] fileArray = new String[versionedFilesArrayList.size()];

        return versionedFilesArrayList.toArray(fileArray);

    }

    /**
     * 新增檔案至 PVCS。
     * 
     * pcli AddFiles -pr"${projectDatabase}" -id"${username}:${password}"
     * -pp"${projectPath}" -c -m"${description}" -t"${description}" -v"${label}"
     * ${files}
     * 
     * @param projectDatabase
     * @param projectPath
     * @param username
     * @param password
     * @param label
     * @param description
     * @param files
     * @return
     */
    @Override
    public int[] addFiles(String projectDatabase, String projectPath, String username, String password, String label,
            String description, String[] files) {
        // TODO Auto-generated method stub
        int[] rc = new int[files.length];

        String addFileArray = "";
        for (String file : files) {
            addFileArray += " " + file;
        }

        ArrayList<String> addOkFileList = new ArrayList<String>();

        try {
            String command = "pcli AddFiles -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password
                    + "\" -pp\"" + projectPath + "\" -c -m\"" + description + "\" -t\"" + description + "\" -v\""
                    + label + "\" " + addFileArray;
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String file = "";
            while ((file = bf.readLine()) != null) {
                addOkFileList.add(file);
            }
            bf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // test
        for (String s : addOkFileList) {
            logger.debug("add file:" + s);
        }

        // 判斷每個source file是否成功新增
        for (int i = 0; i < files.length; i++) {
            String oriFile = files[i];
            if (addOkFileList.contains(oriFile)) {
                rc[i] = 0;
            } else {
                rc[i] = -1;
            }
        }

        return rc;
    }

    /**
     * 新增檔案至 PVCS。
     * 
     * pcli AddFiles -pr"${projectDatabase}" -id"${username}:${password}"
     * -pp"${projectPath}" -c -m"${description}" -t"${description}" -v"${label}"
     * ${files}
     * 
     * @param projectDatabase
     * @param projectPath
     * @param username
     * @param password
     * @param label
     * @param description
     * @param files
     * @return
     */
    @Override
    public Map<String, Object> addFiles(String projectDatabase, String projectPath, String username, String password,
            String label, String description, String path, Set<String> files) {
        int rc = 0;
        String result = "";
        try {
            String command = "pcli AddFiles -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password
                    + "\" -pp\"" + projectPath + "\" -z -m\"" + description + "\" -t\"" + description + "\" -v\""
                    + label + "\" " + path;
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);
            StreamHandler stdStream = new StreamHandler(process.getInputStream(), "STDOUT", files);
            stdStream.start();
            StreamHandler errStream = new StreamHandler(process.getErrorStream(), "ERROR", files);
            errStream.start();
            process.waitFor();
            result = "\n\nERROR Detail:\n" + errStream.getResult() + "\n\nOUTPUT Detail:\n" + stdStream.getResult();
            if ("".equals(errStream.getResult().trim())) {
                rc = process.exitValue();
            } else {
                rc = -2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rc = -1;
        }
        Map<String, Object> out = new HashMap<String, Object>();
        out.put("rc", rc);
        out.put("result", result);
        return out;
    }

    /**
     * 將先前被 check out 的檔案，check in 回 PVCS。
     * 
     * pcli Put -pr"${projectDatabase}" -id"${username}:${password}"
     * -pp"${projectPath}" -v"${label}" -m"${description}" ${files}
     * 
     * @param projectDatabase
     * @param projectPath
     * @param username
     * @param password
     * @param label
     * @param description
     * @param files
     * @return
     */
    @Override
    public int[] putFiles(String projectDatabase, String projectPath, String username, String password, String label,
            String description, String[] files) {

        int[] rc = new int[files.length];

        String addFileArray = "";
        for (String file : files) {
            addFileArray += " " + file;
        }

        ArrayList<String> addOkFileList = new ArrayList<String>();

        try {
            String command = "pcli AddFiles -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password
                    + "\" -pp\"" + projectPath + "\" -c -m\"" + description + "\" -t\"" + description + "\" -v\""
                    + label + "\" " + addFileArray;
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String file = "";
            while ((file = bf.readLine()) != null) {
                addOkFileList.add(file);
            }
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // test
        for (String s : addOkFileList) {
            logger.debug("add file:" + s);
        }

        // 判斷每個source file是否成功Check in
        for (int i = 0; i < files.length; i++) {
            String oriFile = files[i];
            if (addOkFileList.contains(oriFile)) {
                rc[i] = 0;
            } else {
                rc[i] = -1;
            }
        }

        return rc;
    }

    @Override
    public Map<String, Object> putFiles(String projectDatabase, String projectPath, String username, String password,
            String label, String description, String path, Set<String> files) {

        int rc = 0;
        String result = "";

        try {
            String command = "pcli Put -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password + "\" -pp\""
                    + projectPath + "\" -z -m\"" + description + "\" -v\"" + label + "\" -bp\"" + path + "\" *";
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);
            StreamHandler stdStream = new StreamHandler(process.getInputStream(), "STDOUT", files);
            stdStream.start();
            StreamHandler errStream = new StreamHandler(process.getErrorStream(), "ERROR", files);
            errStream.start();
            process.waitFor();
            result = "\n\nERROR Detail:\n" + errStream.getResult() + "\n\nOUTPUT Detail:\n" + stdStream.getResult();
            if ("".equals(errStream.getResult().trim())) {
                rc = process.exitValue();
            } else {
                rc = -2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rc = -1;
        }
        Map<String, Object> out = new HashMap<String, Object>();
        out.put("rc", rc);
        out.put("result", result);
        return out;
    }

    /**
     * 刪除 PVCS 中的檔案。
     * 
     * pcli Delete -pr"${projectDatabase}" -id"${username}:${password}"
     * -pp"${projectPath}" ${files}
     * 
     * @param projectDatabase
     * @param projectPath
     * @param username
     * @param password
     * @param files
     * @return
     */
    @Override
    public int[] deleteFiles(String projectDatabase, String projectPath, String username, String password,
            String[] files) {

        int[] rc = new int[files.length];

        String delFileArray = "";
        for (String file : files) {
            delFileArray += " " + file;
        }

        ArrayList<String> delFileList = new ArrayList<String>();

        try {
            String command = "pcli Delete -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password
                    + "\" -pp\"" + projectPath + "\" " + delFileArray;
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String file = "";
            while ((file = bf.readLine()) != null) {
                delFileList.add(file);
            }
            bf.close();
            // int r = process.exitValue();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // test
        for (String s : delFileList) {
            logger.debug("del file:" + s);
        }

        // 判斷每個soirce file是否成功刪除
        for (int i = 0; i < files.length; i++) {
            String oriFile = files[i];
            if (delFileList.contains(oriFile)) {
                rc[i] = 0;
            } else {
                rc[i] = -1;
            }
        }

        return rc;
    }

    /**
     * 把檔案從 PVCS 中 check out 出來。
     * 
     * pcli Get -pr"${projectDatabase}" -id"${username}:${password}"
     * -pp"${projectPath}" -l -nm -o ${file}
     * 
     * @param projectDatabase
     * @param projectPath
     * @param username
     * @param password
     * @param files
     * @return
     */
    @Override
    public int[] getFiles(String projectDatabase, String projectPath, String username, String password, String[] files) {

        int[] rc = new int[files.length];

        String checkoutFileArray = "";
        for (String file : files) {
            checkoutFileArray += " " + file;
        }

        ArrayList<String> checkoutFileList = new ArrayList<String>();

        try {
            String command = "pcli get -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password + "\" -pp\""
                    + projectPath + "\" -l -nm -o " + checkoutFileArray;
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);
            /*
             * BufferedReader bf = new BufferedReader(new
             * InputStreamReader(process.getInputStream())); String file="";
             * while((file=bf.readLine()) != null){ checkoutFileList.add(file);
             * } bf.close();
             */
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // test
        for (String s : checkoutFileList) {
            logger.debug("checkout file:" + s);
        }

        // 判斷每個soirce file是否成功check out
        /*
         * for(int i=0; i<files.length; i++){ String oriFile=files[i];
         * if(checkoutFileList.contains(oriFile)){ rc[i] = 0; }else{ rc[i] = -1;
         * } }
         */
        return rc;
    }

    /**
     * 比較兩個檔案。
     * 
     * vdiff ${file1} ${file2}
     * 
     * @param path1
     * @param path2
     * @return
     */
    @Override
    public String diff(String path1, String path2, String file) {
        String diffResult = "";
        try {
            File temp = FileUtil.prepareTempDirectory();
            Bundle b = Platform.getBundle("tw.com.citi.catalog.web");

            URL resourceUrl = b.getResource("tw/com/citi/catalog/web/util/impl/diff");
            URL fileUrl = FileLocator.toFileURL(resourceUrl);
            File hiddenFile = new File(fileUrl.toURI());
            File realDiffFile = new File(temp, hiddenFile.getName() + ".exe");
            FileCopyUtils.copy(hiddenFile, realDiffFile);

            resourceUrl = b.getResource("tw/com/citi/catalog/web/util/impl/msys-1.0.dll");
            fileUrl = FileLocator.toFileURL(resourceUrl);
            hiddenFile = new File(fileUrl.toURI());
            File realMSysFile = new File(temp, hiddenFile.getName());
            FileCopyUtils.copy(hiddenFile, realMSysFile);

            String command = realDiffFile.getAbsolutePath() + " -N -r " + path1 + " " + path2 + " > C:\\Temp\\" + file + ".diff";
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);

            StreamHandler stdStream = new StreamHandler(process.getInputStream(), "STDOUT", false);
            stdStream.start();
            StreamHandler errStream = new StreamHandler(process.getErrorStream(), "ERROR", false);
            errStream.start();
            process.waitFor();
            diffResult = "\n\nERROR Detail:\n" + errStream.getResult() + "\n\nOUTPUT Detail:\n" + stdStream.getResult();
        } catch (Exception e) {
            logger.error("Failed to compare files.", e);
            throw new RuntimeException(e);
        } finally {
            FileUtil.deleteTempDirectory();
        }

        return diffResult;
    }

}
