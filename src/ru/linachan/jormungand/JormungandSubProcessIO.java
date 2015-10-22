package ru.linachan.jormungand;

import ru.linachan.yggdrasil.YggdrasilCore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JormungandSubProcessIO implements Runnable {

    private YggdrasilCore core;
    private JormungandSubProcess process;
    private InputStream outputStream;
    private List<String> outputLog;

    public JormungandSubProcessIO(YggdrasilCore core, JormungandSubProcess process, InputStream outputStream) {
        this.core = core;
        this.process = process;
        this.outputStream = outputStream;
        this.outputLog = new ArrayList<>();
    }

    @Override
    public void run() {
        while (process.isRunning()) {
            try {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(outputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    outputLog.add(line);
                }

                reader.close();
            } catch (final Exception e) {
                // core.logException(e);
                // Ignore exception
            }
        }
    }

    public List<String> getOutput() {
        return outputLog;
    }
}
