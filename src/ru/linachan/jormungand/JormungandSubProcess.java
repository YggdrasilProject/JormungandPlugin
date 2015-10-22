package ru.linachan.jormungand;

import ru.linachan.yggdrasil.YggdrasilCore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JormungandSubProcess {

    private ProcessBuilder processBuilder;
    private YggdrasilCore yggdrasilCore;
    private Map<String, String> processEnvironment;
    private Process process;
    private JormungandSubProcessIO subProcessIO;
    private InputStream processOutput;
    private Integer returnCode;
    private Boolean isRunning = false;

    private List<String> processTags = new ArrayList<>();

    private JormungandSubProcessState processState;

    public JormungandSubProcess(YggdrasilCore yggdrasilCore, String... cmd) {
        this.yggdrasilCore = yggdrasilCore;
        this.processBuilder = new ProcessBuilder(cmd);
        this.processEnvironment = processBuilder.environment();

        this.processBuilder.redirectErrorStream(true);

        this.processState = JormungandSubProcessState.READY;
    }

    public JormungandSubProcess(YggdrasilCore yggdrasilCore, List<String> cmd) {
        this.yggdrasilCore = yggdrasilCore;
        this.processBuilder = new ProcessBuilder(cmd);
        this.processEnvironment = processBuilder.environment();

        this.processBuilder.redirectErrorStream(true);

        this.processState = JormungandSubProcessState.READY;
    }

    public void tagProcess(String tag) {
        this.processTags.add(tag);
    }

    public boolean hasTag(String tag) {
        return this.processTags.contains(tag);
    }

    public void clearEnvironment() {
        this.processEnvironment.clear();
    }

    public void setEnvironment(String key, String value) {
        this.processEnvironment.put(key, value);
    }

    public void setEnvironment(Map<String, String> map) {
        this.processEnvironment.putAll(map);
    }

    public void unsetEnvironment(String key) {
        this.processEnvironment.remove(key);
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.processBuilder.directory(workingDirectory);
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.processBuilder.directory(new File(workingDirectory));
    }

    public void run() {
        try {
            processState = JormungandSubProcessState.WAITING;

            process = processBuilder.start();
            isRunning = true;

            processState = JormungandSubProcessState.RUNNING;

            processOutput = process.getInputStream();

            subProcessIO = new JormungandSubProcessIO(yggdrasilCore, this, processOutput);
            new Thread(subProcessIO).start();

            process.waitFor();

            returnCode = process.exitValue();

            process.destroy();

            processState = JormungandSubProcessState.FINISHED;
        } catch(InterruptedException | IOException e) {
            processState = JormungandSubProcessState.ERROR;
            returnCode = 1;
            this.yggdrasilCore.logException(e);
        } finally {
            isRunning = false;
        }
    }

    public void kill() {
        process.destroy();
    }

    public List<String> getProcessOutput() {
        return subProcessIO.getOutput();
    }

    public JormungandSubProcessState getState() {
        return processState;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public boolean isFinished() {
        return (processState == JormungandSubProcessState.FINISHED)||(processState == JormungandSubProcessState.ERROR);
    }

    public Boolean isRunning() {
        return isRunning;
    }
}
