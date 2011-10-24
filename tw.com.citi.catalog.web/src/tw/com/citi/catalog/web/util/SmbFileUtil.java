package tw.com.citi.catalog.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs.FileFilter;
import org.apache.commons.vfs.FileFilterSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.local.LocalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.com.citi.catalog.conf.Jcifs;
import tw.com.citi.catalog.web.vfs.OSGiFileSystemManager;

public class SmbFileUtil {

    static final Logger logger = LoggerFactory.getLogger(SmbFileUtil.class);
    private static boolean init = false;
    private static FileSystemManager fsManager;
    private static FileSystemOptions opts;
    private static final int BUFF_SIZE = 100000;
    private static final byte[] buffer = new byte[BUFF_SIZE];

    public static void init() {
        try {
            String username = Jcifs.getUsername();
            String domain = Jcifs.getDomain();
            StaticUserAuthenticator auth = new StaticUserAuthenticator(domain, username, Jcifs.getUserpassword());
            opts = new FileSystemOptions();
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
            fsManager = new OSGiFileSystemManager();
            ((OSGiFileSystemManager) fsManager).init();
            // get "Primary WINS Server" & "Host Name"
            Socket s = new Socket();
            String hostName = s.getLocalAddress().getHostName();
            String wins = "";
            Process process = null;
            BufferedReader bf = null;
            String command = "ipconfig /all";
            String[] cmd = new String[] { "cmd", "/C", command };
            process = Runtime.getRuntime().exec(cmd);
            bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = bf.readLine()) != null) {
                if (line.indexOf("Primary WINS Server") >= 0) {
                    wins = line.substring(line.lastIndexOf(":") + 1).trim();
                }
                if (line.indexOf("Host Name") >= 0) {
                    hostName = line.substring(line.lastIndexOf(":") + 1).trim();
                }
            }
            bf.close();
            logger.debug("Host Name : " + hostName);
            logger.debug("WINS      : " + wins);
            if (!"".equalsIgnoreCase(wins)) {
                jcifs.Config.setProperty("jcifs.netbios.wins", wins);
            }
            jcifs.Config.setProperty("jcifs.netbios.hostname", hostName);
            init = true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException("Load folder settings error.", e);
        }
    }

    public static List<FileObject> getFiles(String sourceFolder) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        FileObject source = fsManager.resolveFile("smb:" + replaceSlash(sourceFolder), opts);
        return getFiles(source);
    }

    private static List<FileObject> getFiles(FileObject source) throws FileSystemException {
        List<FileObject> files = new ArrayList<FileObject>();
        if (FileType.FOLDER == source.getType()) {
            FileObject[] objects = source.getChildren();
            for (FileObject object : objects) {
                files.addAll(getFiles(object));
            }
        } else {
            files.add(source);
        }
        return files;
    }

    public static void copyFolder(String sourceFolder, String targetFolder) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        FileObject source = fsManager.resolveFile("smb:" + replaceSlash(sourceFolder), opts);
        List<FileObject> files = new ArrayList<FileObject>();
        FileObject[] objects = source.getChildren();
        for (FileObject object : objects) {
            files.addAll(getFiles(object));
        }
        for (FileObject file : files) {
            final String name = file.getName().getBaseName();
            String path = file.getParent().getName().getURI();
            String t = path.replace(replaceSlash(sourceFolder), replaceSlash(targetFolder));
            FileObject target = fsManager.resolveFile(t, opts);
            target.copyFrom(file.getParent(), new FileFilterSelector(new FileFilter() {
                @Override
                public boolean accept(FileSelectInfo info) {
                    if (name.contains(info.getFile().getName().getBaseName())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }));
        }
    }

    public static void copyFile(FileObject source, FileObject target, String prefix, String suffix,
            String[] sourceFileNames) throws FileSystemException {
        List<FileObject> files = new ArrayList<FileObject>();
        if (sourceFileNames == null || sourceFileNames.length == 0) {
            // 複製全部檔案
            FileObject[] objects = source.getChildren();
            files.addAll(Arrays.asList(objects));
        } else {
            for (String fileName : sourceFileNames) {
                FileObject file = fsManager.resolveFile(source, fileName);
                if (file != null) {
                    files.add(file);
                }
            }
        }
        if (files != null && files.size() != 0) {
            final List<String> accept = new ArrayList<String>();
            for (FileObject file : files) {
                accept.add(file.getName().getBaseName());
            }
            FileFilterSelector ffs = new FileFilterSelector(new FileFilter() {
                @Override
                public boolean accept(FileSelectInfo info) {
                    if (accept.contains(info.getFile().getName().getBaseName())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            target.copyFrom(source, ffs);
            if (prefix != null || suffix != null) {
                // 修改檔名
                if (target.getFileSystem() instanceof LocalFileSystem) {
                    List<File> targetFiles = new ArrayList<File>();
                    for (String fileName : sourceFileNames) {
                        File file = new File(target.getURL().getFile() + "/" + fileName);
                        if (file != null && file.exists()) {
                            targetFiles.add(file);
                        }
                    }
                    if (targetFiles != null || targetFiles.size() > 0) {
                        for (File file : targetFiles) {
                            String baseName = file.getName();
                            String extensions = baseName.indexOf(".") > 0 ? baseName.substring(baseName.indexOf("."))
                                    : "";
                            String fileName = baseName.indexOf(".") > 0 ? baseName.substring(0,
                                    baseName.indexOf(".") - 1) : baseName;
                            StringBuffer newName = new StringBuffer();
                            newName = newName.append(prefix == null ? "" : prefix).append(fileName)
                                    .append(suffix == null ? "" : suffix).append(extensions);
                            file.renameTo(new File(target.getURL().getFile() + "/" + newName.toString()));
                        }
                    }
                } else {
                    List<FileObject> targetFiles = new ArrayList<FileObject>();
                    for (String fileName : sourceFileNames) {
                        FileObject file = fsManager.resolveFile(target, fileName);
                        if (file != null) {
                            targetFiles.add(file);
                        }
                    }
                    if (targetFiles != null || targetFiles.size() > 0) {
                        for (FileObject file : targetFiles) {
                            String baseName = file.getName().getBaseName();
                            String extensions = baseName.indexOf(".") > 0 ? baseName.substring(baseName.indexOf("."))
                                    : "";
                            String fileName = baseName.indexOf(".") > 0 ? baseName.substring(0,
                                    baseName.indexOf(".") - 1) : baseName;
                            StringBuffer newName = new StringBuffer();
                            newName = newName.append(prefix == null ? "" : prefix).append(fileName)
                                    .append(suffix == null ? "" : suffix).append(extensions);
                            FileObject temp = fsManager.resolveFile(target, newName.toString());
                            temp.createFile();
                            file.moveTo(temp);
                        }
                    }
                }
            }
        }
    }

    public static void copyFile(String sourceFolder, String targetFolder, String prefix, String suffix,
            String[] sourceFileNames) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        if (sourceFolder == null) {
            throw new IllegalArgumentException("input source folder is invalid.");
        }
        if (targetFolder == null) {
            throw new IllegalArgumentException("input target folder is invalid.");
        }
        FileObject source = fsManager.resolveFile("smb:" + replaceSlash(sourceFolder), opts);
        FileObject target = fsManager.resolveFile("smb:" + replaceSlash(targetFolder), opts);
        copyFile(source, target, prefix, suffix, sourceFileNames);
    }

    public static void copyLocalToSmb(String sourceFolder, String targetFolder, String[] sourceFileNames)
            throws FileSystemException {
        copyLocalToSmb(sourceFolder, targetFolder, null, null, sourceFileNames);
    }

    public static void copyLocalToSmb(String sourceFolder, String targetFolder, String prefix, String suffix,
            String[] sourceFileNames) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        if (sourceFolder == null) {
            throw new IllegalArgumentException("input source folder is invalid.");
        }
        if (targetFolder == null) {
            throw new IllegalArgumentException("input target folder is invalid.");
        }
        FileObject source = fsManager.resolveFile("file://" + replaceSlash(sourceFolder), opts);
        FileObject target = fsManager.resolveFile("smb:" + replaceSlash(targetFolder), opts);
        copyLocalToSmb(source, target, prefix, suffix, sourceFileNames);
    }

    public static void copyLocalToSmb(FileObject source, FileObject target, String prefix, String suffix,
            String[] sourceFileNames) throws FileSystemException {
        List<FileObject> files = new ArrayList<FileObject>();
        if (sourceFileNames == null || sourceFileNames.length == 0) {
            // 複製全部檔案
            FileObject[] objects = source.getChildren();
            files.addAll(Arrays.asList(objects));
        } else {
            for (String fileName : sourceFileNames) {
                FileObject file = fsManager.resolveFile(source, fileName);
                if (file != null) {
                    files.add(file);
                }
            }
        }
        if (files != null && files.size() != 0) {
            final List<String> accept = new ArrayList<String>();
            for (FileObject file : files) {
                accept.add(file.getName().getBaseName());
            }
            FileFilterSelector ffs = new FileFilterSelector(new FileFilter() {
                @Override
                public boolean accept(FileSelectInfo info) {
                    if (accept.contains(info.getFile().getName().getBaseName())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            target.copyFrom(source, ffs);
            if (prefix != null || suffix != null) {
                // 修改檔名
                if (target.getFileSystem() instanceof LocalFileSystem) {
                    List<File> targetFiles = new ArrayList<File>();
                    for (String fileName : sourceFileNames) {
                        File file = new File(target.getURL().getFile() + "/" + fileName);
                        if (file != null && file.exists()) {
                            targetFiles.add(file);
                        }
                    }
                    if (targetFiles != null || targetFiles.size() > 0) {
                        for (File file : targetFiles) {
                            String baseName = file.getName();
                            String extensions = baseName.indexOf(".") > 0 ? baseName.substring(baseName.indexOf("."))
                                    : "";
                            String fileName = baseName.indexOf(".") > 0 ? baseName.substring(0,
                                    baseName.indexOf(".") - 1) : baseName;
                            StringBuffer newName = new StringBuffer();
                            newName = newName.append(prefix == null ? "" : prefix).append(fileName)
                                    .append(suffix == null ? "" : suffix).append(extensions);
                            file.renameTo(new File(target.getURL().getFile() + "/" + newName.toString()));
                        }
                    }
                } else {
                    List<FileObject> targetFiles = new ArrayList<FileObject>();
                    for (String fileName : sourceFileNames) {
                        FileObject file = fsManager.resolveFile(target, fileName);
                        if (file != null) {
                            targetFiles.add(file);
                        }
                    }
                    if (targetFiles != null || targetFiles.size() > 0) {
                        for (FileObject file : targetFiles) {
                            String baseName = file.getName().getBaseName();
                            String extensions = baseName.indexOf(".") > 0 ? baseName.substring(baseName.indexOf("."))
                                    : "";
                            String fileName = baseName.indexOf(".") > 0 ? baseName.substring(0,
                                    baseName.indexOf(".") - 1) : baseName;
                            StringBuffer newName = new StringBuffer();
                            newName = newName.append(prefix == null ? "" : prefix).append(fileName)
                                    .append(suffix == null ? "" : suffix).append(extensions);
                            FileObject temp = fsManager.resolveFile(target, newName.toString());
                            temp.createFile();
                            file.moveTo(temp);
                        }
                    }
                }
            }
        }
    }

    public static void copyFile(String sourceFolder, String targetFolder, String[] sourceFileNames)
            throws FileSystemException {
        copyFile(sourceFolder, targetFolder, null, null, sourceFileNames);
    }

    public static void copySmbToLocal(String sourceFolder, String targetFolder, String prefix, String suffix,
            String[] sourceFileNames) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        if (sourceFolder == null) {
            throw new IllegalArgumentException("input source folder is invalid.");
        }
        if (targetFolder == null) {
            throw new IllegalArgumentException("input target folder is invalid.");
        }
        FileObject source = fsManager.resolveFile("smb:" + replaceSlash(sourceFolder), opts);
        FileObject target = fsManager.resolveFile("file://" + replaceSlash(targetFolder), opts);
        copySmbToLocal(source, target, prefix, suffix, sourceFileNames);
    }

    public static void copySmbToLocal(String sourceFolder, String targetFolder, String[] sourceFileNames)
            throws FileSystemException {
        copySmbToLocal(sourceFolder, targetFolder, null, null, sourceFileNames);
    }

    public static void copySmbToLocal(FileObject source, FileObject target, String prefix, String suffix,
            String[] sourceFileNames) throws FileSystemException {
        List<FileObject> files = new ArrayList<FileObject>();
        if (sourceFileNames == null || sourceFileNames.length == 0) {
            // 複製全部檔案
            FileObject[] objects = source.getChildren();
            files.addAll(Arrays.asList(objects));
        } else {
            for (String fileName : sourceFileNames) {
                FileObject file = fsManager.resolveFile(source, fileName);
                if (file != null) {
                    files.add(file);
                }
            }
        }
        if (files != null && files.size() != 0) {
            final List<String> accept = new ArrayList<String>();
            for (FileObject file : files) {
                accept.add(file.getName().getBaseName());
            }
            FileFilterSelector ffs = new FileFilterSelector(new FileFilter() {
                @Override
                public boolean accept(FileSelectInfo info) {
                    if (accept.contains(info.getFile().getName().getBaseName())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            target.copyFrom(source, ffs);
            if (prefix != null || suffix != null) {
                // 修改檔名
                if (target.getFileSystem() instanceof LocalFileSystem) {
                    List<File> targetFiles = new ArrayList<File>();
                    for (String fileName : sourceFileNames) {

                        // logger.debug("src url:"+source.getURL().getFile());

                        File file = new File(target + "\\" + fileName);
                        if (file != null && file.exists()) {
                            targetFiles.add(file);
                        }
                    }
                    if (targetFiles != null || targetFiles.size() > 0) {
                        for (File file : targetFiles) {
                            String baseName = file.getName();
                            String extensions = baseName.indexOf(".") > 0 ? baseName.substring(baseName.indexOf("."))
                                    : "";
                            String fileName = baseName.indexOf(".") > 0 ? baseName.substring(0,
                                    baseName.indexOf(".") - 1) : baseName;
                            StringBuffer newName = new StringBuffer();
                            newName = newName.append(prefix == null ? "" : prefix).append(fileName)
                                    .append(suffix == null ? "" : suffix).append(extensions);
                            file.renameTo(new File(target + "\\" + newName.toString()));
                        }
                    }
                } else {
                    List<FileObject> targetFiles = new ArrayList<FileObject>();
                    for (String fileName : sourceFileNames) {
                        FileObject file = fsManager.resolveFile(target, fileName);
                        if (file != null) {
                            targetFiles.add(file);
                        }
                    }
                    if (targetFiles != null || targetFiles.size() > 0) {
                        for (FileObject file : targetFiles) {
                            String baseName = file.getName().getBaseName();
                            String extensions = baseName.indexOf(".") > 0 ? baseName.substring(baseName.indexOf("."))
                                    : "";
                            String fileName = baseName.indexOf(".") > 0 ? baseName.substring(0,
                                    baseName.indexOf(".") - 1) : baseName;
                            StringBuffer newName = new StringBuffer();
                            newName = newName.append(prefix == null ? "" : prefix).append(fileName)
                                    .append(suffix == null ? "" : suffix).append(extensions);
                            FileObject temp = fsManager.resolveFile(target, newName.toString());
                            temp.createFile();
                            file.moveTo(temp);
                        }
                    }
                }
            }
        }
    }

    public static void uploadFile(InputStream in, String path, String fileName) throws IOException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(path), opts);
        FileObject file = fsManager.resolveFile(folder, fileName);
        file.createFile();
        OutputStream out = null;
        try {
            out = file.getContent().getOutputStream(false);
            while (true) {
                synchronized (buffer) {
                    int amountRead = in.read(buffer);
                    if (amountRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, amountRead);
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static boolean exist(String path, String fileName) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        boolean tf = false;
        FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(path), opts);
        if (fileName != null) {
            FileObject file = fsManager.resolveFile(folder, fileName);
            tf = file.exists();
        } else {
            tf = folder.exists();
        }
        return tf;
    }

    /**
     * 新增目錄。
     * 
     * @param filePath
     * @throws FileSystemException
     */
    public static void createFolder(String filePath) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(filePath), opts);
        folder.createFolder();
    }

    /**
     * 新增一個檔案，若檔案已經存在，會刪除既有檔案。
     * 
     * @param path
     * @param fileName
     * @throws FileSystemException
     */
    public static void createFile(String path, String fileName) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(path), opts);
        FileObject file = fsManager.resolveFile(folder, fileName);
        if (exist(path, fileName)) {
            file.delete();
        }
        file.createFile();
    }

    public static void deleteFile(String path, String fileName) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(path), opts);
        if (fileName == null) {
            if (exist(path, null)) {
                folder.delete();
            }
        } else {
            FileObject file = fsManager.resolveFile(folder, fileName);
            if (exist(path, fileName)) {
                file.delete();
            }
        }
    }

    public static FileObject getFile(String filePath, String fileName) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(filePath), opts);
        FileObject file = fsManager.resolveFile(folder, fileName);
        return file;
    }

    public static List<FileObject> listBatchFiles(String filePath) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        List<FileObject> files = new ArrayList<FileObject>();
        FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(filePath), opts);
        FileObject[] allFiles = folder.getChildren();
        for (FileObject file : allFiles) {
            String fileName = file.getName().getBaseName();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if ("bat".equalsIgnoreCase(extension)) {
                files.add(file);
            }
        }
        return files;
    }

    public static List<FileObject> listFiles(String filePath) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(filePath), opts);
        return Arrays.asList(folder.getChildren());
    }

    /**
     * 判斷是否對傳入路徑有寫入的權限。若有權限且路徑不存在，此路徑會被建立。
     * 
     * @param filePath
     *            欲判斷之路徑
     * @return 是否有權限寫入
     */
    public static boolean writeable(String filePath) {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        boolean tf = true;
        try {
            FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(filePath), opts);
            tf = folder.isWriteable();
        } catch (FileSystemException e) {
            e.printStackTrace();
            tf = false;
        }
        return tf;
    }

    public static boolean accessCheck(String filePath) throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(filePath), opts);
        return folder.isWriteable();
    }

    public static void renameTo(String oldPath, String oldName, String newPath, String newName)
            throws FileSystemException {
        if (!init) {
            throw new RuntimeException("Please config Folder Access Information in JCS5000.");
        }
        if (oldName == null) {
            FileObject sourceFolder = fsManager.resolveFile("smb:" + replaceSlash(oldPath), opts);
            FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(newPath), opts);
            folder.createFolder();
            sourceFolder.moveTo(folder);
        } else {
            FileObject sourceFolder = fsManager.resolveFile("smb:" + replaceSlash(oldPath), opts);
            FileObject sourceFile = fsManager.resolveFile(sourceFolder, oldName);
            FileObject folder = fsManager.resolveFile("smb:" + replaceSlash(newPath), opts);
            FileObject temp = fsManager.resolveFile(folder, newName);
            temp.createFile();
            sourceFile.moveTo(temp);
        }
    }

    private static String replaceSlash(String path) {
        return path.replace("\\", "/");
    }
}
