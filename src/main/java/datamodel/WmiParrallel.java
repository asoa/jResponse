package datamodel;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class WmiParrallel extends Service<String> {
    // instance vars
    private String buttonName;
    private ObservableList<PingParrallel.PingResult> pingResults;
//    public final static int THREADCOUNT = Runtime.getRuntime().availableProcessors();
    private int THREADCOUNT = 50;
    private ExecutorService pool;
    private List<Callable<String>> callables;
    private String command;
    private HashMap<String, String> wmiResults;
    private String wmiString;
    private List<Future<String>> futures;
    private List<String> futureResults;
    private boolean isDone = false;


    // constructor
    public WmiParrallel(String buttonName, String command, ObservableList<PingParrallel.PingResult> pingResults) {
        this.pingResults = pingResults;
        this.buttonName = buttonName;
        this.command = command;
        pool = Executors.newFixedThreadPool(THREADCOUNT);
        callables = new ArrayList<>();
        futures = new ArrayList<>();
        futureResults = new ArrayList<>();
    }

    public WmiParrallel() {}

    public String getFutureResults() {
        return futureResults.toString();
    }

    public boolean isDone() {
        return isDone;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {
            Future<String> future;
            @Override
            protected String call() throws Exception {

                try {
                    for (PingParrallel.PingResult result : pingResults) {
//                        callables.add(new PSCommand(result, command));
                         future = pool.submit(new PSCommand(result, command));
                         futureResults.add(future.get());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                futures = pool.invokeAll(callables);  //  succeeds here with successful state
//                for(Future<String> future: futures) {
//                    futureResults.add(future.get());
////                    System.out.println(future.get());
//                }
//                return futureResults.toString();  // still empty
                return future.get();
            }

        };
    }

    class PSCommand implements Callable<String> {
        // instance vars
        private String ip;
        private String hostname;
        private PingParrallel.PingResult result;
        private String output;
        private BufferedReader stdout;
        // constructor
        public PSCommand(PingParrallel.PingResult result, String command) {
            this.ip = result.getIpAddress();
            this.hostname = result.getHostname();
            this.result = result;
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

            } catch (Exception e) {
                e.printStackTrace();
            }
//            wmiResults.put(result.getHostname(), output);
            return toString();
        }

//        @Override
//        public String toString() {
//            return "PSCommand{" +
//                    "buttonName=" + buttonName + '\'' +
//                    "ip='" + ip + '\'' +
//                    ", hostname='" + hostname + '\'' +
//                    ", output=" + output +
//                    '}';
//        }

        @Override
        public String toString() {
            String strFormat = "##### %s: %s #####\n" +
                    output + "\n";
            String result = String.format(strFormat, buttonName, hostname);
            System.out.println(result);
            return result;
        }
    }
}
