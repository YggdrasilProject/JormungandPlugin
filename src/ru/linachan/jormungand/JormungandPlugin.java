package ru.linachan.jormungand;

import ru.linachan.yggdrasil.component.YggdrasilPlugin;

import java.util.*;

public class JormungandPlugin extends YggdrasilPlugin {

    private Map<Long, JormungandSubProcess> processList = new HashMap<>();
    private JormungandExecutor processExecutor;

    @Override
    protected void setUpDependencies() {

    }

    @Override
    protected void onInit() {
        processExecutor = new JormungandExecutor();
        core.getServiceManager().startService(processExecutor);
    }

    @Override
    protected void onShutdown() {

    }

    @Override
    public boolean executeTests() {
        Long pid = prepareExecution("ls", "-l");

        scheduleExecution(pid);

        JormungandSubProcess process = waitFor(pid);

        Integer retVal = process.getReturnCode();

        if (retVal > 0) {
            core.logWarning("EXIT_CODE: " + retVal);
            for (String line : process.getProcessOutput()) {
                core.logWarning("OUT: " + line);
            }
        }

        return retVal == 0;
    }

    private Long generateProcessID() {
        Random randomGenerator = new Random();
        while (true) {
            Long pid = Math.abs(randomGenerator.nextLong());
            if (!processList.containsKey(pid))
                return pid;
        }
    }

    public JormungandSubProcess getProcess(Long processID) {
        if (processList.containsKey(processID))
            return processList.get(processID);
        return null;
    }

    public Map<Long, JormungandSubProcess> getProcessesByTag(String tag) {
        Map<Long, JormungandSubProcess> processes = new HashMap<>();
        for (Long processID : processList.keySet()) {
            if (processList.get(processID).hasTag(tag)) {
                processes.put(processID, processList.get(processID));
            }
        }
        return processes;
    }

    public JormungandSubProcess waitFor(Long processID) {
        JormungandSubProcess process = getProcess(processID);
        while (!process.isFinished()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                core.logException(e);
            }
        }
        return process;
    }

    public Long prepareExecution(String... cmd) {
        JormungandSubProcess subProcess = new JormungandSubProcess(core, cmd);
        Long subProcessID = generateProcessID();

        processList.put(subProcessID, subProcess);
        return subProcessID;
    }

    public Long prepareExecution(List<String> cmd) {
        JormungandSubProcess subProcess = new JormungandSubProcess(core, cmd);
        Long subProcessID = generateProcessID();

        processList.put(subProcessID, subProcess);
        return subProcessID;
    }

    public void scheduleExecution(Long subProcessID) {
        processExecutor.schedule(subProcessID);
    }
}
