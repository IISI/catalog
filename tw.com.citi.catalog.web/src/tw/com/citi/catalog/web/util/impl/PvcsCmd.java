package tw.com.citi.catalog.web.util.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param file
     * @return
     */
    @Override
    public int addFile(String projectDatabase, String projectPath, String username, String password, String label,
            String description, String file) {
        String command = "pcli AddFiles -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password + "\" -pp\""
                + projectPath + "\" -m\"" + description + "\" -t\"" + description + "\" -v\"" + label + "\" " + file;
        logger.debug("command:" + command);
        String[] cmd = new String[] { "cmd", "/C", command };
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            return process.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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
     * @param file
     * @return
     */
    @Override
    public int putFile(String projectDatabase, String projectPath, String username, String password, String label,
            String description, String file) {
        try {
            String command = "pcli Put -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password + "\" -pp\""
                    + projectPath + "\" -m\"" + description + "\" -v\"" + label + "\" " + file;
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            return process.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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
     * @param file
     * @return
     */
    @Override
    public int deleteFile(String projectDatabase, String projectPath, String username, String password, String file) {

        try {
            String command = "pcli Delete -pr\"" + projectDatabase + "\" -id\"" + username + ":" + password
                    + "\" -pp\"" + projectPath + "\" " + file;
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            return process.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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
     * @param file1
     * @param file2
     * @return
     */
    @Override
    public String diff(String file1, String file2) {

        String diffResult = "";
        try {
            String command = "vdiff " + file1 + " " + file2;
            logger.debug("command:" + command);
            String[] cmd = new String[] { "cmd", "/C", command };
            Process process = Runtime.getRuntime().exec(cmd);

            BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String r = "";
            while ((r = bf.readLine()) != null) {
                diffResult += r + "\n";
            }
            bf.close();

            if (diffResult.trim().equals("")) {
                BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String err = "";
                while ((err = errReader.readLine()) != null) {
                    diffResult += err + "\n";
                }
                bf.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return diffResult;
    }

}
