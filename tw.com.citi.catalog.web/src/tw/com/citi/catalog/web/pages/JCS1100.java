package tw.com.citi.catalog.web.pages;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbAuthException;

import org.apache.commons.vfs.FileSystemException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.FileSystemResource;
import org.springframework.validation.BindException;

import tw.com.citi.catalog.util.DateUtil;
import tw.com.citi.catalog.util.HashUtil;
import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.dao.IAppFileDao;
import tw.com.citi.catalog.web.dao.IAppPathDao;
import tw.com.citi.catalog.web.dao.IBuildUnitDao;
import tw.com.citi.catalog.web.dao.ICoordinatorDao;
import tw.com.citi.catalog.web.dao.IProgrammerDao;
import tw.com.citi.catalog.web.dao.IRegisterHistoryDao;
import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.dao.IScrFileDao;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.model.App;
import tw.com.citi.catalog.web.model.AppFile;
import tw.com.citi.catalog.web.model.AppPath;
import tw.com.citi.catalog.web.model.AppPath.PathType;
import tw.com.citi.catalog.web.model.BuildUnit;
import tw.com.citi.catalog.web.model.Coordinator;
import tw.com.citi.catalog.web.model.Programmer;
import tw.com.citi.catalog.web.model.Scr;
import tw.com.citi.catalog.web.model.ScrFile;
import tw.com.citi.catalog.web.util.FileUtil;
import tw.com.citi.catalog.web.util.IPvcsCmd;
import tw.com.citi.catalog.web.util.IZipCmd;
import tw.com.citi.catalog.web.util.SmbFileUtil;
import tw.com.citi.catalog.web.util.impl.PvcsCmd;
import tw.com.citi.catalog.web.util.impl.ZipCmd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JCS1100 extends AbstractBasePage {

    @SpringBean(name = "scrDao")
    private IScrDao scrDao;

    @SpringBean(name = "appDao")
    private IAppDao appDao;

    @SpringBean(name = "coordinatorDao")
    private ICoordinatorDao coordinatorDao;

    @SpringBean(name = "programmerDao")
    private IProgrammerDao programmerDao;

    @SpringBean(name = "buildUnitDao")
    private IBuildUnitDao buildUnitDao;

    @SpringBean(name = "appPathDao")
    private IAppPathDao appPathDao;

    @SpringBean(name = "scrFileDao")
    private IScrFileDao scrFileDao;

    @SpringBean(name = "appFileDao")
    private IAppFileDao appFileDao;

    @SpringBean(name = "registerHistoryDao")
    private IRegisterHistoryDao registerHistoryDao;

    @SpringBean(name = "scrFileGrid")
    private IGridHandler scrFileGrid;

    private transient Gson gson = new Gson();

    @Override
    public IGridHandler getGridHandler(String handlerName) {
        if ("scrFileGrid".equals(handlerName)) {
            return scrFileGrid;
        }
        return super.getGridHandler(handlerName);
    }

    @Override
    public String handleRequest(PageParameters params) throws IOException {
        String actionName = params.getString("actionName");
        if ("findScrNo".equals(actionName)) {
            return findScrNo();
        } else if ("getScrInfo".equals(actionName)) {
            Long scrId = params.getAsLong("actionParams[scrId]");
            return getScrInfo(scrId);
        } else if ("findBuildUnit".equals(actionName)) {
            Long scrId = params.getAsLong("scrId");
            return findBuildUnit(scrId);
        } else if ("unzipFile".equals(actionName)) {
            String zipFile = params.getString("actionParams[zipFile]");
            String zipPassword = params.getString("actionParams[zipPassword");
            return unzipFile(zipFile, zipPassword);
        } else if ("checkImportFile".equals(actionName)) {
            String changeFile = params.getString("actionParams[changeFile]");
            String hashFile = params.getString("actionParams[hashFile]");
            String pvcsUsername = params.getString("actionParams[pvcsUsername]");
            String pvcsPassword = params.getString("actionParams[pvcsPassword]");
            long scrId = params.getLong("actionParams[scrId]");
            return checkImportFile(changeFile, hashFile, pvcsUsername, pvcsPassword, scrId);
        } else if ("register".equals(actionName)) {
            long scrId = params.getLong("actionParams[scrId]");
            String objs = params.getString("actionParams[files]");
            GsonBuilder builder = new GsonBuilder();
            builder.setDateFormat(DateUtil.FORMAT);
            List<JCS1100.FileModel> files = builder.create().fromJson(objs, new TypeToken<List<JCS1100.FileModel>>() {}.getType());
            return register(scrId, files);
        }
        return null;
    }

    private String findScrNo() {
        List<Scr> scrList = scrDao.findAll();
        StringBuilder sb = new StringBuilder();
        for (Scr scr : scrList) {
            sb.append("<option value='").append(scr.getId()).append("'>");
            sb.append(scr.getScrNo());
            sb.append("</option>");
        }
        return sb.toString();
    }

    private String findBuildUnit(Long scrId) {
        Scr scr = scrDao.findById(scrId);
        List<BuildUnit> buildUnitList = buildUnitDao.findByAppId(scr.getJcAppId());
        StringBuilder sb = new StringBuilder();
        sb.append("<option></option>");
        for (BuildUnit unit : buildUnitList) {
            sb.append("<option value='").append(unit.getId()).append("'>");
            sb.append(unit.getUnitId());
            sb.append("</option>");
        }
        return sb.toString();
    }

    private String getScrInfo(Long scrId) {
        Scr scr = scrDao.findById(scrId);
        App app = appDao.findById(scr.getJcAppId());
        Coordinator coordinator = coordinatorDao.findById(scr.getJcCoordinatorId());
        Programmer programmer = programmerDao.findById(scr.getJcProgrammerId());
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("scrNo", scr.getScrNo());
        results.put("appId", app.getAppId());
        results.put("status", scr.getStatus().name());
        results.put("submitTime", DateUtil.format(scr.getProcessTime()));
        results.put("librarian", "");
        results.put("coordinator", coordinator.getName());
        results.put("programmer", programmer.getName());
        results.put("createTime", DateUtil.format(scr.getCreateTime()));
        results.put("description", scr.getDescription());
        return gson.toJson(results);
    }

    private String unzipFile(String zipFile, String zipPassword) {
        File tempDir = FileUtil.prepareTempDirectory();
        IZipCmd zipCmd = new ZipCmd();
        int result = zipCmd.unzip(zipFile, tempDir.getAbsolutePath(), zipPassword);
        Map<String, Object> results = new HashMap<String, Object>();
        if (result == 0) {
            results.put("success", true);
        } else {
            results.put("success", false);
        }
        return gson.toJson(results);
    }

    private String checkImportFile(String changeFile, String hashFile, String pvcsUsername, String pvcsPassword, long scrId) {
        ResultModel result = new ResultModel();
        result.setQualified(true);
        
        // parse imported change file
        Map<String, JCS1100.FileModel> files = readFile(changeFile);
        result.setFiles(files.values());
        
        // parse hash info file
        readHashFile(hashFile, files);
        
        // start to check files
        File tempDir = FileUtil.getTempDirectory();
        AppPath appPath = appPathDao.findByScrId(scrId, PathType.APP_BASE).get(0);
        String rdPath = appPath.getPath().endsWith("\\") || appPath.getPath().endsWith("/") ? appPath.getPath().concat("RD\\") : appPath.getPath().concat("\\RD\\");
        App app = appDao.findById(appPath.getJcAppId());
        IPvcsCmd pvcsCmd = new PvcsCmd();
        String[] versionedFiles = pvcsCmd.listVersionedFiles(app.getPvcsProjDb(), app.getPvcsProjPath(), pvcsUsername, pvcsPassword, rdPath);
        for (JCS1100.FileModel file : files.values()) {
            try {
                if ("New".equalsIgnoreCase(file.getAction())) {
                    if (SmbFileUtil.exist(rdPath + file.getSourcePath(), file.getSourceFileName())) {
                        file.setError(true);
                        result.setQualified(false);
                    }
                    if (file.isCheckin() && isFileVersioned(versionedFiles, file.getSourcePath() + file.getSourceFileName())) {
                        file.setError(true);
                        result.setQualified(false);
                    }
                } else if ("Update".equalsIgnoreCase(file.getAction())) {
                    if (!SmbFileUtil.exist(rdPath + file.getSourcePath(), file.getSourceFileName())) {
                        file.setError(true);
                        result.setQualified(false);
                    }
                    ScrFile scrFile = scrFileDao.findByUK(scrId, file.getSourcePath(), file.getSourceFileName());
                    if (file.isCheckin() && isFileVersioned(versionedFiles, file.getSourcePath() + file.getSourceFileName())) {
                        if (!scrFile.getCheckout()) {
                            file.setError(true);
                            result.setQualified(false);
                        }
                    }
                } else if ("Delete".equalsIgnoreCase(file.getAction())) {
                    if (!SmbFileUtil.exist(rdPath + file.getSourcePath(), file.getSourceFileName())) {
                        file.setError(true);
                        result.setQualified(false);
                    }
                    //TODO 若Checkin為Y，檔案必須存在PVCS且為Checkout狀態；或是檔案不存在PVCS。不確定沒Checkout是否能從PVCS刪除，待測。
                }
            } catch (FileSystemException e) {
                logger.error("Failed to check RD file.", e);
                file.setError(true);
                result.setQualified(false);
                if (e.getCause() instanceof SmbAuthException) {
                    break;
                }
            }
            
            try {
                // check md5
                File tempfile = new File(tempDir, file.getSourcePath() + file.getSourceFileName());
                if (tempfile.exists()) {
                    file.setInZip(true);
                    file.setFileSize(tempfile.length());
                    file.setFileDatetime(new Date(tempfile.lastModified()));
                    String md5 = HashUtil.getMD5Checksum(tempfile);
                    if (!md5.equalsIgnoreCase(file.getExpectedMd5())) {
                        file.setError(true);
                        result.setQualified(false);
                    }
                    file.setFileMd5(md5);
                } else {
                    file.setInZip(false);
                }
            } catch (Exception e) {
                file.setError(true);
                result.setQualified(false);
            }
        }
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(DateUtil.FORMAT);
        return gsonBuilder.create().toJson(result);
    }

    private boolean isFileVersioned(String[] versionedFiles, String file) {
        for (String versionedFile : versionedFiles) {
            if (versionedFile.equalsIgnoreCase(file)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, JCS1100.FileModel> readFile(String changeFile) {
        FlatFileItemReader<JCS1100.FileModel> itemReader = new FlatFileItemReader<JCS1100.FileModel>();
        itemReader.setResource(new FileSystemResource(changeFile));
        DefaultLineMapper<JCS1100.FileModel> lineMapper = new DefaultLineMapper<JCS1100.FileModel>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer('|');
        tokenizer.setNames(new String[] {"action", "checkin", "buildUnit", "sourcePath", "sourceFileName", "executionPath", "executionFileName"});
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new FileFieldSetMapper());
        itemReader.setLineMapper(lineMapper);
        itemReader.open(new ExecutionContext());
        Map<String, JCS1100.FileModel> files = new HashMap<String, JCS1100.FileModel>();
        try {
            for (JCS1100.FileModel fileModel = itemReader.read(); fileModel != null; fileModel = itemReader.read()) {
                files.put(fileModel.getBuildUnit() + "|" + fileModel.getSourcePath() + "|" + fileModel.getSourceFileName(), fileModel);
            }
        } catch (UnexpectedInputException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        itemReader.close();
        return files;
    }

    private void readHashFile(String hashFile, Map<String, JCS1100.FileModel> files) {
        FlatFileItemReader<JCS1100.HashFileModel> itemReader = new FlatFileItemReader<JCS1100.HashFileModel>();
        itemReader.setResource(new FileSystemResource(hashFile));
        DefaultLineMapper<JCS1100.HashFileModel> lineMapper = new DefaultLineMapper<JCS1100.HashFileModel>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer('|');
        tokenizer.setNames(new String[] {"buildUnit", "sourcePath", "sourceFileName", "md5"});
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new HashFileFieldSetMapper());
        itemReader.setLineMapper(lineMapper);
        itemReader.open(new ExecutionContext());
        try {
            for (JCS1100.HashFileModel hashFileModel = itemReader.read(); hashFileModel != null; hashFileModel = itemReader.read()) {
                String key = hashFileModel.getBuildUnit() + "|" + hashFileModel.getSourcePath() + "|" + hashFileModel.getSourceFileName();
                JCS1100.FileModel file = files.get(key);
                if (file != null) {
                    file.setExpectedMd5(hashFileModel.getMd5());
                }
            }
        } catch (UnexpectedInputException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String register(long scrId, List<JCS1100.FileModel> files) throws IOException {
        Scr scr = scrDao.findById(scrId);
        Map<String, Object> updateMap = new HashMap<String, Object>();
        updateMap.put("id", scr.getId());
        updateMap.put("registerCount", scr.getRegisterCount() + 1);
        updateMap.put("status", Scr.Status.REGISTER.ordinal());
        updateMap.put("processTime", new Date());
        scrDao.update1100(updateMap);
        
        for (JCS1100.FileModel file : files) {
            updateMap.clear();
            
            AppFile appSourceFile = appFileDao.findByUK(scr.getJcAppId(), file.getSourcePath(), file.getSourceFileName());
            AppFile appExecutionFile = appFileDao.findByUK(scr.getJcAppId(), file.getExecutionPath(), file.getExecutionFileName());
            ScrFile scrSourceFile = scrFileDao.findByUK(scr.getId(), file.getSourcePath(), file.getSourceFileName());
            ScrFile scrExecutionFile = scrFileDao.findByUK(scr.getId(), file.getExecutionPath(), file.getExecutionFileName());
            
            updateMap.put("JC_APP_ID", scr.getJcAppId());
            updateMap.put("JC_BUILD_UNIT_ID", buildUnitDao.findByAppId(scr.getJcAppId()).get(0).getId());
            updateMap.put("LAST_REGISTER_TIME", new Date());
            
            updateMap.put("JC_SCR_ID", scr.getId());
            
            updateMap.put("REGISTER_COUNT", scr.getRegisterCount() + 1);
            updateMap.put("CHECK_IN", file.isCheckin());
            updateMap.put("REGISTER_TIME", new Date());
            
            if ("new".equalsIgnoreCase(file.getAction())) {
                // create app source file
                updateMap.put("FILE_PATH", file.getSourcePath());
                updateMap.put("FILE_NAME", file.getSourceFileName());
                updateMap.put("FILE_TYPE", ScrFile.FileType.SOURCE.ordinal());
                updateMap.put("FILE_DATETIME", file.getFileDatetime());
                updateMap.put("FILE_SIZE", file.getFileSize());
                updateMap.put("FILE_MD5", file.getFileMd5());
                updateMap.put("CHECKOUT", false);
                updateMap.put("DELETED", 0);
                if (appSourceFile == null) {
                    appFileDao.create(updateMap);
                } else if (appSourceFile.getDeleted()) {
                    appFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                // create scr source file
                Long scrSourceFileId = null;
                if (scrSourceFile == null) {
                    scrSourceFileId = scrFileDao.create(updateMap);
                } else if (scrSourceFile.getDeleted()) {
                    scrFileDao.update1100(updateMap);
                    scrSourceFileId = scrSourceFile.getId();
                } else {
                    throw new RuntimeException();
                }
                
                // create source register history
                updateMap.put("JC_SCR_FILE_ID", scrSourceFileId);
                updateMap.put("REGISTER_ACTION", 0);
                registerHistoryDao.create(updateMap);
                
                // create app execution file
                updateMap.put("FILE_PATH", file.getExecutionPath());
                updateMap.put("FILE_NAME", file.getExecutionFileName());
                updateMap.put("FILE_TYPE", ScrFile.FileType.EXECUTION.ordinal());
                updateMap.put("FILE_DATETIME", null);
                updateMap.put("FILE_SIZE", null);
                updateMap.put("FILE_MD5", null);
                if (appExecutionFile == null) {
                    appFileDao.create(updateMap);
                } else if (appExecutionFile.getDeleted()) {
                    appFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                //create scr execution file
                Long scrExecutionFileId = null;
                if (scrExecutionFile == null) {
                    scrExecutionFileId = scrFileDao.create(updateMap);
                } else if (scrExecutionFile.getDeleted()) {
                    scrFileDao.update1100(updateMap);
                    scrExecutionFileId = scrExecutionFile.getId();
                } else {
                    throw new RuntimeException();
                }
                
                // create execution register history
                updateMap.put("JC_SCR_FILE_ID", scrExecutionFileId);
                registerHistoryDao.create(updateMap);
            } else if ("update".equalsIgnoreCase(file.getAction())) {
                // update app source file
                if (appSourceFile != null && !appSourceFile.getDeleted()) {
                    updateMap.put("FILE_TYPE", ScrFile.FileType.SOURCE.ordinal());
                    updateMap.put("FILE_DATETIME", file.getFileDatetime());
                    updateMap.put("FILE_SIZE", file.getFileSize());
                    updateMap.put("FILE_MD5", file.getFileMd5());
                    updateMap.put("ID", appSourceFile.getId());
                    updateMap.put("CHECKOUT", appSourceFile.getCheckout());
                    updateMap.put("DELETED", appSourceFile.getDeleted());
                    appFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                // update scr source file
                if (scrSourceFile != null && !scrSourceFile.getDeleted()) {
                    scrFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                // create source register history
                updateMap.put("JC_SCR_FILE_ID", scrSourceFile.getId());
                updateMap.put("REGISTER_ACTION", 1);
                registerHistoryDao.create(updateMap);
                
                // update app execution file
                if (appExecutionFile != null && !appExecutionFile.getDeleted()) {
                    updateMap.put("FILE_TYPE", ScrFile.FileType.EXECUTION.ordinal());
                    updateMap.put("FILE_DATETIME", null);
                    updateMap.put("FILE_SIZE", null);
                    updateMap.put("FILE_MD5", null);
                    updateMap.put("ID", appExecutionFile.getId());
                    updateMap.put("CHECKOUT", appExecutionFile.getCheckout());
                    updateMap.put("DELETED", appExecutionFile.getDeleted());
                    appFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                // update scr execution file
                if (scrExecutionFile != null && !scrExecutionFile.getDeleted()) {
                    scrFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                // create execution register history
                updateMap.put("JC_SCR_FILE_ID", scrExecutionFile.getId());
                registerHistoryDao.create(updateMap);
            } else if ("delete".equalsIgnoreCase(file.getAction())) {
                // update app source file
                if (appSourceFile != null && !appSourceFile.getDeleted()) {
                    updateMap.put("FILE_TYPE", ScrFile.FileType.SOURCE.ordinal());
                    updateMap.put("FILE_DATETIME", file.getFileDatetime());
                    updateMap.put("FILE_SIZE", file.getFileSize());
                    updateMap.put("FILE_MD5", file.getFileMd5());
                    updateMap.put("DELETED", 1);
                    updateMap.put("ID", appSourceFile.getId());
                    updateMap.put("CHECKOUT", appSourceFile.getCheckout());
                    appFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                // update scr source file
                if (scrSourceFile != null && !scrSourceFile.getDeleted()) {
                    scrFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                // create source register history
                updateMap.put("JC_SCR_FILE_ID", scrSourceFile.getId());
                updateMap.put("REGISTER_ACTION", 1);
                registerHistoryDao.create(updateMap);
                
                // update app execution file
                if (appExecutionFile != null && !appExecutionFile.getDeleted()) {
                    updateMap.put("FILE_TYPE", ScrFile.FileType.EXECUTION.ordinal());
                    updateMap.put("FILE_DATETIME", null);
                    updateMap.put("FILE_SIZE", null);
                    updateMap.put("FILE_MD5", null);
                    updateMap.put("DELETED", 1);
                    updateMap.put("ID", appExecutionFile.getId());
                    updateMap.put("CHECKOUT", appExecutionFile.getCheckout());
                    appFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                // update scr execution file
                if (scrExecutionFile != null && !scrExecutionFile.getDeleted()) {
                    scrFileDao.update1100(updateMap);
                } else {
                    throw new RuntimeException();
                }
                
                // create execution register history
                updateMap.put("JC_SCR_FILE_ID", scrExecutionFile.getId());
                registerHistoryDao.create(updateMap);
            }
            
            FileInputStream fi = null;
            try {
                AppPath appPath = appPathDao.findByScrId(scrId, PathType.APP_BASE).get(0);
                String rdPath = appPath.getPath().endsWith("\\") || appPath.getPath().endsWith("/") ? appPath.getPath().concat("RD\\") : appPath.getPath().concat("\\RD\\");
                if ("new".equalsIgnoreCase(file.getAction()) || "update".equalsIgnoreCase(file.getAction())) {
                    File tempFile = new File(FileUtil.getTempDirectory(), file.getSourcePath() + file.getSourceFileName());
                    fi = new FileInputStream(tempFile);
                    BufferedInputStream bi = new BufferedInputStream(fi);
                    SmbFileUtil.uploadFile(bi, rdPath + file.getSourcePath(), file.getSourceFileName());
                } else if ("delete".equalsIgnoreCase(file.getAction())) {
                    SmbFileUtil.deleteFile(rdPath + file.getSourcePath(), file.getSourceFileName());
                }
            } catch (FileNotFoundException e) {
                logger.error("Failed to copy/delete files.", e);
                throw e;
            } catch (FileSystemException e) {
                logger.error("Failed to copy/delete files.", e);
                throw e;
            } catch (IOException e) {
                logger.error("Failed to copy/delete files.", e);
                throw e;
            } finally {
                if (fi != null) {
                    fi.close();
                }
            }
        }
        return null;
    }

    private class FileModel {

        private String fileName;

        private String sourcePath;

        private String sourceFileName;

        private String buildUnit;

        private long fileSize;

        private Date fileDatetime;

        private String action;

        private boolean inZip;

        private String expectedMd5;

        private String fileMd5;

        private String executionPath;

        private String executionFileName;

        private boolean checkin;

        private boolean error;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getSourcePath() {
            return sourcePath;
        }

        public void setSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
        }

        public String getSourceFileName() {
            return sourceFileName;
        }

        public void setSourceFileName(String sourceFileName) {
            this.sourceFileName = sourceFileName;
        }

        public String getBuildUnit() {
            return buildUnit;
        }

        public void setBuildUnit(String buildUnit) {
            this.buildUnit = buildUnit;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public Date getFileDatetime() {
            return fileDatetime;
        }

        public void setFileDatetime(Date fileDatetime) {
            this.fileDatetime = fileDatetime;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public boolean isInZip() {
            return inZip;
        }

        public void setInZip(boolean inZip) {
            this.inZip = inZip;
        }

        public String getExpectedMd5() {
            return expectedMd5;
        }

        public void setExpectedMd5(String expectedMd5) {
            this.expectedMd5 = expectedMd5;
        }

        public String getFileMd5() {
            return fileMd5;
        }

        public void setFileMd5(String fileMd5) {
            this.fileMd5 = fileMd5;
        }

        public String getExecutionPath() {
            return executionPath;
        }

        public void setExecutionPath(String executionPath) {
            this.executionPath = executionPath;
        }

        public String getExecutionFileName() {
            return executionFileName;
        }

        public void setExecutionFileName(String executionFileName) {
            this.executionFileName = executionFileName;
        }

        public boolean isCheckin() {
            return checkin;
        }

        public void setCheckin(boolean checkin) {
            this.checkin = checkin;
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
        }

    }

    private class HashFileModel {

        private String buildUnit;

        private String sourcePath;

        private String sourceFileName;

        private String md5;

        public String getBuildUnit() {
            return buildUnit;
        }

        public void setBuildUnit(String buildUnit) {
            this.buildUnit = buildUnit;
        }

        public String getSourcePath() {
            return sourcePath;
        }

        public void setSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
        }

        public String getSourceFileName() {
            return sourceFileName;
        }

        public void setSourceFileName(String sourceFileName) {
            this.sourceFileName = sourceFileName;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

    }

    private class ResultModel {

        private boolean qualified;

        private Collection<JCS1100.FileModel> files;

        public boolean isQualified() {
            return qualified;
        }

        public void setQualified(boolean qualified) {
            this.qualified = qualified;
        }

        public Collection<JCS1100.FileModel> getFiles() {
            return files;
        }

        public void setFiles(Collection<JCS1100.FileModel> files) {
            this.files = files;
        }

    }

    private class FileFieldSetMapper implements FieldSetMapper<FileModel> {

        @Override
        public FileModel mapFieldSet(FieldSet fs) throws BindException {
            FileModel file = new FileModel();
            if ("N".equalsIgnoreCase(fs.readString("action"))) {
                file.setAction("New");
            } else if ("O".equalsIgnoreCase(fs.readString("action"))) {
                file.setAction("Update");
            } else if ("D".equalsIgnoreCase(fs.readString("action"))) {
                file.setAction("Delete");
            }
            file.setBuildUnit(fs.readString("buildUnit"));
            file.setCheckin(fs.readBoolean("checkin", "Y"));
            file.setExecutionFileName(fs.readString("executionFileName"));
            String executionPath = fs.readString("executionPath");
            executionPath = executionPath.endsWith("\\") || executionPath.endsWith("/") ? executionPath : executionPath.concat("\\");
            file.setExecutionPath(executionPath);
            file.setSourceFileName(fs.readString("sourceFileName"));
            String sourcePath = fs.readString("sourcePath");
            sourcePath = sourcePath.endsWith("\\") || sourcePath.endsWith("/") ? sourcePath : sourcePath.concat("\\");
            file.setSourcePath(sourcePath);
            file.setFileName(file.getSourcePath() + file.getSourceFileName());
            return file;
        }

    }

    private class HashFileFieldSetMapper implements FieldSetMapper<HashFileModel> {

        @Override
        public HashFileModel mapFieldSet(FieldSet fs) throws BindException {
            HashFileModel file = new HashFileModel();
            file.setBuildUnit(fs.readString("buildUnit"));
            String sourcePath = fs.readString("sourcePath");
            sourcePath = sourcePath.endsWith("\\") || sourcePath.endsWith("/") ? sourcePath : sourcePath.concat("\\");
            file.setSourcePath(sourcePath);
            file.setSourceFileName(fs.readString("sourceFileName"));
            file.setMd5(fs.readString("md5"));
            return file;
        }

    }

}
