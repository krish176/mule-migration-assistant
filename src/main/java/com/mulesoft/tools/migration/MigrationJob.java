package com.mulesoft.tools.migration;


import com.mulesoft.tools.migration.builder.TaskBuilder;
import com.mulesoft.tools.migration.exception.MigrationJobException;
import com.mulesoft.tools.migration.report.ConsoleReportStrategy;
import com.mulesoft.tools.migration.report.ReportCategory;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.task.MigrationTask;
import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.tools.migration.report.ReportCategory.WORKING_WITH_FILE;

public class MigrationJob {

    public static final String TASK_FIELD = "tasks";
    public static final String BACKUP_FOLDER = "backup";

    private List<String> filePaths;
    private String filesDir;
    private String configFilePath;
    private String configFileDir;
    private List<MigrationTask> tasks = new ArrayList<>();
    private Boolean backup = false;
    private Boolean onErrorStop;
    private File destFolder = new File(BACKUP_FOLDER);
    private Document document;
    private ReportingStrategy reportingStrategy;

    public void addTask(MigrationTask task) {
        this.tasks.add(task);
    }

    public void execute() throws Exception {

        if (null != configFilePath) {
            parseConfigurationFile(configFilePath);
        } else {
            parseConfigurationFiles(configFileDir);
        }

        if (null != this.filesDir) {
            this.filePaths = getXmlPaths(this.filesDir);
        }

        if (backup) {
            saveCopyOfFiles(filePaths);
        }

        try {
            for (String filePath : this.filePaths){
                this.document = generateDoc(filePath);
                getReportingStrategy().log(filePath, WORKING_WITH_FILE);
                for (MigrationTask task : tasks) {
                    task.setReportingStrategy(this.reportingStrategy);
                    task.setDocument(this.document);
                    task.setOnErrorStop(this.onErrorStop);
                    task.execute();
                }
                XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
                xmlOutputter.output(this.document, new FileOutputStream(filePath));
            }
        } catch (Exception ex) {
            throw new MigrationJobException("Failed to migrate the file: " + this.document.getBaseURI() + ". " + ex.getMessage() + "/n" + ex.getStackTrace());
        }
    }

    public Document generateDoc(String filePath) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        File file = new File(filePath);
        return saxBuilder.build(file);
    }

    public void parseConfigurationFile(String configFilePath) throws Exception {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(configFilePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONArray tasks = (JSONArray) jsonObject.get(TASK_FIELD);

            for (Object task : tasks) {
                JSONObject taskObj = (JSONObject) task;
                this.addTask(TaskBuilder.build(taskObj));
            }

        } catch (Exception ex) {
            throw new Exception("Failed to parse Configuration file " + this.configFilePath + ". " + ex.getMessage());
        }
    }

    public void parseConfigurationFiles(String rootDirectoryPath) throws Exception {
        try {
            File rootDirectory = new File(rootDirectoryPath);

            List<File> files = (List<File>) FileUtils.listFiles(rootDirectory, new String[] { "json" }, true);

            for (File file : files) {
                parseConfigurationFile(file.getAbsolutePath());
            }

        } catch (Exception ex) {
            throw new Exception("Failed to parse Configuration files" + rootDirectoryPath + ". " + ex.getMessage());
        }
    }

    public List<String> getXmlPaths(String rootDirectoryPath) throws Exception {

        File rootDirectory = new File(rootDirectoryPath);

        List<File> files = (List<File>) FileUtils.listFiles(rootDirectory, new String[] { "xml" }, true);

        List<String> filesPaths = new ArrayList<>();
        for (File file : files) {
            filesPaths.add(file.getAbsolutePath());
        }

        return filesPaths;
    }

    public void saveCopyOfFiles(List<String> filePaths) throws Exception{
        try {
            for (String filePath : filePaths) {
                File copyFile = new File(filePath);

                //Path targetPath = destFolder.toPath().resolve(copyFile.getParent());
                Path targetPath = Paths.get(copyFile.getParentFile().getAbsolutePath() + File.separator +
                        BACKUP_FOLDER + File.separator + copyFile.getName());
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                }

                Files.copy(copyFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            throw new Exception("Failed to backup files. Exception: " + ex.getMessage() + " " + ex.getStackTrace());
        }
    }

    public void setBackUpProfile(Boolean backUpProfile) {
        this.backup = backUpProfile;
    }

    public void setConfigFilePath(String configFile) {
        this.configFilePath = configFile;
    }

    public void setDocuments(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public void setFilesDir(String filesDir) {
        this.filesDir = filesDir;
    }

    public void setConfigFileDir(String configFileDir) {
        this.configFileDir = configFileDir;
    }

    public void setReportingStrategy(ReportingStrategy reportingStrategy) {
        this.reportingStrategy = reportingStrategy;
    }

    public void setOnErrorStop(Boolean onErrorStop) {
        this.onErrorStop = onErrorStop;
    }

    public ReportingStrategy getReportingStrategy() {
        if (null == this.reportingStrategy) {
            this.reportingStrategy = new ConsoleReportStrategy();
        }
        return reportingStrategy;
    }
}
