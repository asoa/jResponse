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
        this.command = command;
        pool = Executors.newFixedThreadPool(THREADCOUNT);
        callables = new ArrayList<>();
//        futures = new ArrayList<>();
        futures = new ArrayList<>();
//        futureResults = new ArrayList<String>();
        futureResults = new ArrayList<>();
        wmiResults = new HashMap<>();
        scripts = new WmiScripts();
    }

    public WmiParrallel() {}

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
//                        future = pool.submit(new PSCommand(result, command));  // don't delete this works
//                        futureResults.add(future.get() + "\n");
                        callables.add(new PSCommand(result, command));

                    }
                } catch (Exception e) {
                    System.out.println("Error in createTask" + e);
                }
                futures = pool.invokeAll(callables);

                for(Future<String> future: futures) {
                    futureResults.add(future.get() + "\n");  // used to print output to text area in enumeration tab
//                    wmiResults.put(future.)
                }

                return futureResults.toString();
//                return future;

            }

        };
    }

//    class PSCommand implements Callable<String> {
    class PSCommand implements Callable<String> {
        // instance vars
        private String ip;
        private String hostname;
        private PingParrallel.PingResult result;
//        private String output;
        private List<String> output;
        private BufferedReader stdout;
        List<String> results;
        // constructor
        public PSCommand(PingParrallel.PingResult result, String command) {
            this.result = result;
            this.ip = this.result.getIpAddress();
            this.hostname = this.result.getHostname();
            output = new ArrayList<>();
            results = new ArrayList<>();
        }

//        public String call() {
    public String call() {
            try {
                Process powershellProcess = Runtime.getRuntime().exec("powershell.exe " + command);
                powershellProcess.getOutputStream().close();  // closes the process output stream to prepare for buffered stream
                stdout = new BufferedReader(new InputStreamReader(powershellProcess.getInputStream()));
                String line;
//                String results="";
//                List<String> results = new ArrayList<>();
                while((line = stdout.readLine()) != null) {
//                    results += line + "\n";
                    results.add(line + "\n");
//                    System.out.println(line);
//                    output.add(line);
                }
//                output.add(results);
                wmiResults.put(hostname, results);  // add key:value to dict; used to write to db
                stdout.close();  // close the stream


            } catch (Exception e) {
                System.out.println("Error in call(): " + e);
                e.printStackTrace();
            }
//            wmiResults.put(result.getHostname(), output);
//            return toString();
            return toString();
        }

        @Override
        public String toString() {
            String strFormat = "##### %s: %s #####\n" + results;
            String result = String.format(strFormat, buttonName, hostname);
            System.out.println(result);
            return result;
        }
    }
}
