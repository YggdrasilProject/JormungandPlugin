package ru.linachan.jormungand;

import ru.linachan.yggdrasil.service.YggdrasilService;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class JormungandExecutor extends YggdrasilService {

    private JormungandPlugin executor;
    private Queue<Long> processQueue = new PriorityQueue<>();
    private Semaphore executorLock = new Semaphore(1);

    private Long runningProcess;

    @Override
    public void run() {
        core.logInfo("JormungandExecutor: Starting...");
        try {
            while (core.isRunning()) {
                if (!processQueue.isEmpty()) {
                    executorLock.acquire();
                    if ((runningProcess = processQueue.remove()) != null) {

                        JormungandSubProcess subProcess = executor.getProcess(runningProcess);

                        subProcess.run();

                        core.logInfo("JormungandExecutor: Process ID" + runningProcess + " finished with EXIT_CODE: " + subProcess.getReturnCode());
                        runningProcess = null;
                    }
                    executorLock.release();
                }
                Thread.sleep(100);
            }
        } catch(InterruptedException e) {
            core.logException(e);
            executorLock.release();
        }
        core.logInfo("JormungandExecutor: Stopped");
    }

    @Override
    protected void onInit() {
        executor = (JormungandPlugin) core.getPluginManager().getPlugin(JormungandPlugin.class);
    }

    @Override
    protected void onShutdown() {
        if (runningProcess != null) {
            executor.getProcess(runningProcess).kill();
        }
    }

    public void schedule(Long subProcessID) {
        processQueue.add(subProcessID);
    }
}
