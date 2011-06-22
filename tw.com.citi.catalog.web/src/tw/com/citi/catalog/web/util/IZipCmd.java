package tw.com.citi.catalog.web.util;

public interface IZipCmd {

    /**
     * 解壓縮檔案至指定的位置。
     * 
     * pkzipc -extract=all -overwirte=all -times=all -directories -silent -passphrase=${password} ${sourceFile} ${targetPath}
     * 
     * @param sourceFile
     * @param targetPath
     * @param password
     * @return
     */
    int unzip(String sourceFile, String targetPath, String password);

    /**
     * 把 source files 壓縮成 zip file。
     * 
     * pkzipc -add -passphrase=${password} -cryptalgorithm=aes,256 -cd=encrypt ${zipFile} ${sourceFiles}
     * 
     * @param zipFile
     * @param sourceFile
     * @param password
     * @return
     */
    int zip(String zipFile, String[] sourceFile, String password);

}
