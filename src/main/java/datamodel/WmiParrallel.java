package datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
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
    private Map<String, List<String>> wmiResults;
    private String wmiString;
    private List<Future<String>> futures;
    private List<String> futureResults;
    private WmiScripts scripts;
    private boolean isDone = false;


    // constructor
    public WmiParrallel(String buttonName, ObservableList<PingParrallel.PingResult> pingResults) {
        this.pingResults = pingResults;
        this.buttonName = buttonName;
        pool = Executors.newFixedThreadPool(THREADCOUNT);
        callables = new ArrayList<>();
//        futures = new ArrayList<>();
        futures = new ArrayList<>();
//        futureResults = new ArrayList<String>();
        futureResults = new ArrayList<>();
        wmiResults = new HashMap<>();
        scripts = new WmiScripts();
    }

    // getters
    public String getFutureResults() {
        return futureResults.toString();
    }

    public Map<String, List<String>> getWmiResults() {  // returns dict of hostname:wmiResults
        return wmiResults;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {
            Future<String> future;
            @Override
            protected String call() throws Exception {

                try {
                    for (PingParrallel.PingResult result : pingResults) {
                        command = scripts.getScript(buttonName, result.getHostname());
                        callables.add(new PSCommand(result, command));

                    }
                } catch (Exception e) {
                    System.out.println((char)27 + "[31m" + "Error in createTask" + e);
                }
                futures = pool.invokeAll(callables);

                for(Future<String> future: futures) {
                    futureResults.add(future.get() + "\n");  // used to print output to text area in enumeration tab
                }

                return futureResults.toString();
            }

        };
    }
//    class PSCommand implements Callable<String> {
    class PSCommand implements Callable<String> {
        // instance vars
        private String ip;
        private String hostname;
        private PingParrallel.PingResult result;
        private List<String> output;
        private BufferedReader stdout;
        List<String> results;
        private String command;
        // constructor
        public PSCommand(PingParrallel.PingResult result, String command) {
            this.result = result;
            this.ip = this.result.getIpAddress();
            this.hostname = this.result.getHostname();
            output = new ArrayList<>();
            results = new ArrayList<>();
            this.command = command;
        }

    public String call() {
            try {
                Process powershellProcess = Runtime.getRuntime().exec("powershell.exe " + command);
                powershellProcess.getOutputStream().close();  // closes the process output stream to prepare for buffered stream
                stdout = new BufferedReader(new InputStreamReader(powershellProcess.getInputStream()));
                String line;
                while((line = stdout.readLine()) != null) {
                    results.add(line + "\n");
                }
                wmiResults.put(hostname, results);  // add key:value to dict; used to write to db
                stdout.close();  // close the stream

            } catch (Exception e) {
                System.out.println((char)27 + "[31m" + "Error in call(): " + e);
                e.printStackTrace();
            }
            return toString();
        }

        @Override
        public String toString() {
            String strFormat = "\n##### %s: %s #####\n" + results;
            String result = String.format(strFormat, buttonName, hostname);
            System.out.printf((char)27 + "[34m");
            System.out.println(result);
            return result;
        }
    }
}
