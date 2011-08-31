package tw.com.citi.catalog.web.util;

public interface IPvcsCmd {

    /**
     * 查詢受到 PVCS 管控的檔案列表。
     * 
     * pcli ListVersionedFiles -pr"${projectDatabase}" -id"${username}:${password}" -pp"${projectPath}" -l -z ${path}
     * 
     * @param projectDatabase
     * @param projectPath
     * @param username
     * @param password
     * @param path
     * @return
     */
    String[] listVersionedFiles(String projectDatabase, String projectPath, String username, String password, String path);

    /**
     * 新增檔案至 PVCS。
     * 
     * pcli AddFiles -pr"${projectDatabase}" -id"${username}:${password}" -pp"${projectPath}" -c -m"${description}" -t"${description}" -v"${label}" ${files}
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
    int[] addFiles(String projectDatabase, String projectPath, String username, String password, String label, String description, String[] files);

    /**
     * 新增檔案至 PVCS。
     * 
     * pcli AddFiles -pr"${projectDatabase}" -id"${username}:${password}" -pp"${projectPath}" -c -m"${description}" -t"${description}" -v"${label}" ${files}
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
    int[] addFiles(String projectDatabase, String projectPath, String username, String password, String label, String description, String path);
    
    /**
     * 將先前被 check out 的檔案，check in 回 PVCS。
     * 
     * pcli Put -pr"${projectDatabase}" -id"${username}:${password}" -pp"${projectPath}" -v"${label}" -m"${description}" ${files}
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
    int[] putFiles(String projectDatabase, String projectPath, String username, String password, String label, String description, String[] files);

    int[] putFiles(String projectDatabase, String projectPath, String username, String password, String label, String description, String path);

    
    /**
     * 刪除 PVCS 中的檔案。
     * 
     * pcli Delete -pr"${projectDatabase}" -id"${username}:${password}" -pp"${projectPath}" ${files}
     * 
     * @param projectDatabase
     * @param projectPath
     * @param username
     * @param password
     * @param files
     * @return
     */
    int[] deleteFiles(String projectDatabase, String projectPath, String username, String password, String[] files);

    /**
     * 把檔案從 PVCS 中 check out 出來。
     * 
     * pcli Get -pr"${projectDatabase}" -id"${username}:${password}" -pp"${projectPath}" -l -nm -o ${file}
     * 
     * @param projectDatabase
     * @param projectPath
     * @param username
     * @param password
     * @param files
     * @return
     */
    int[] getFiles(String projectDatabase, String projectPath, String username, String password, String[] files);

    /**
     * 比較兩個檔案。
     * 
     * vdiff ${file1} ${file2}
     * 
     * @param file1
     * @param file2
     * @return
     */
    String diff(String file1, String file2);

}
