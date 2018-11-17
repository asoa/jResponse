package datamodel;

import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class WmiParrallel {
    // instance vars
    private String buttonName;
    private ObservableList<PingParrallel.PingResult> pingResults;
//    public final static int THREADCOUNT = Runtime.getRuntime().availableProcessors();
    private int THREADCOUNT = 50;
    private final ExecutorService pool;
    private List<Callable<String>> callables;
    private List<Future<String>> futures;
//    private Map<String, String> futureResults;
    private List<String> futureResults;
    private Boolean threadsDone = false;
    private String command;

    // TODO: create List Array<PSCommandResult> for DB entry (done)
    // TODO: create list of Future objects and get status of them to fill javafx textArea (done)
    // TODO: create logic to check if threads are finished (done)
    // TODO: crate hash map for button name -> command
    // TODO: create wmic query
    // TODO: test on WIN-10

    // constructor
    public WmiParrallel(String buttonName, String command, ObservableList<PingParrallel.PingResult> pingResults) {
        this.pingResults = pingResults;
        this.buttonName = buttonName;
        this.pingResults = pingResults;
        this.command = command;
        pool = Executors.newFixedThreadPool(THREADCOUNT);
        callables = new ArrayList<Callable<String>>();  // list of callable objects of type string
        futureResults = new ArrayList<String>();  // results of future
//        futureResults = new HashMap<String, String>();

        try {
            for (PingParrallel.PingResult result : pingResults) {
                callables.add(new PSCommand(result, command));
            }
            futures = pool.invokeAll(callables);  // blocks until all threads are finished
            threadsDone = true;
            for (Future<String> future: futures) {
                futureResults.add(future.get());
//                futureResults.put(future.get())
//                System.out.println(future.get());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean getThreadsDone() {
        return threadsDone;
    }

    public String getFutureResults() {
        String myString = "";
        for(String s: futureResults) {
            myString += s + "\n";
        }
        return myString;
    }

    public void setThreadsDone(Boolean threadsDone) {
        this.threadsDone = threadsDone;
    }

    class PSCommand implements Callable<String> {
        // instance vars
        private String ip;
        private String hostname;
        private String queryResult;
        private PingParrallel.PingResult result;
        private String output;
        private BufferedReader stdout;
        private Map<String, String> wmiResults;

        // constructor
        public PSCommand(PingParrallel.PingResult result, String command) {
            this.ip = result.getIpAddress();
            this.hostname = result.getHostname();
            this.result = result;
            String wmiResult;
        }

        public String call() {
            try {
                Process powershellProcess = Runtime.getRuntime().exec("powershell.exe " + command);
                powershellProcess.getOutputStream().close();  // closes the process output stream to prepare for buffered stream
                stdout = new BufferedReader(new InputStreamReader(powershellProcess.getInputStream()));
                String line;
                String results="";
                while((line = stdout.readLine()) != null) {
                    results += line + "\n";
                    output = results;
//                    System.out.println(line);
                }
                stdout.close();
//                PowerShell powerShell = PowerShell.openSession();
//                PowerShellResponse response = powerShell.executeCommand(command);
//                output = response.getCommandOutput();
//                PowerShell.executeSingleCommand("exit");

            } catch (Exception e) {
                e.printStackTrace();
            }
//            return new PSCommandResult(buttonName, result.getIpAddress(), result.getHostname(), output);
            return toString();
        }

        @Override
        public String toString() {
            return "PSCommand{" +
                    "buttonName=" + buttonName + '\'' +
                    "ip='" + ip + '\'' +
                    ", hostname='" + hostname + '\'' +
                    ", output=" + output +
                    '}';
        }
    }
}
