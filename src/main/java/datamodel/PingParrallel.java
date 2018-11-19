package datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PingParrallel extends Service<ObservableList<PingParrallel.PingResult>> {
    // instance vars
    private boolean state = false;
    private NetworkDiscovery.IP ipObj;
    private final int TIMEOUT = 2000;
//    private int THREADCOUNT = Runtime.getRuntime().availableProcessors();
    private int THREADCOUNT = 50;
    private final ExecutorService pool;
    private List<NetworkDiscovery.IP> hostList;
    private  ObservableList<PingResult> aliveHosts;
    private static ObservableList<String> aliveHostsList;

    // constructor
    public PingParrallel(List<NetworkDiscovery.IP> hostList) {
        this.hostList = hostList;
        pool = Executors.newFixedThreadPool(THREADCOUNT);
        aliveHosts = FXCollections.observableArrayList();
        aliveHostsList = FXCollections.observableArrayList();
//        pool.shutdown();
    }


    // methods
    @Override
    protected Task<ObservableList<PingResult>> createTask() {
        return new Task<>() {
            @Override
            protected ObservableList<PingResult> call() throws Exception {
                try {
                    for(NetworkDiscovery.IP ipObj: hostList) {
                        pool.submit(new Ping(ipObj));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return aliveHosts;  // return observable list to binded value in controller
            }
        };
    }

    public void shutPoolDown() {
        System.out.println("Shutting pool down");
        pool.shutdown();
    }

//    public void buildIPList() {
//        for(PingResult result: aliveHosts) {
//            aliveHostsList.add(result.ipAddress);
//        }
//    }

    // getters and setters
    public ObservableList<PingResult> getAliveHosts() {
        return aliveHosts;
    }

    public List<NetworkDiscovery.IP> getHostList() {
        return hostList;
    }


    // start inner class Ping
    class Ping implements Callable<PingResult> {
        private String ipAddress;
        private String hostname;
        private NetworkDiscovery.IP ipObj;

        public Ping(NetworkDiscovery.IP ipObj) {
            this.ipObj = ipObj;
            this.hostname = "";
            ipAddress = ipObj.getIp();

        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;

        }

        public PingResult call() {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(ipAddress);
                ipObj.setHostname(address.getHostName());
                hostname = ipObj.getHostname();
                state = address.isReachable(TIMEOUT);  // Java implementation of ICMP ECHO REQUEST is sent to address
                if (state) {
                    System.out.println(ipAddress + ": " + hostname + ": " + state);  // debug code
                    aliveHosts.add(new PingResult(address.getHostAddress(), hostname, state));
                    aliveHostsList.add(address.getHostAddress());  // TODO: research using lambda
//                    return new PingResult(address.getHostAddress(), state);
                } else if (!state) {
                    return new PingResult(address.getHostAddress(), hostname, state);
                }
            } catch (UnknownHostException e) {
                System.out.println("UnknownHostException " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IOException " + address.getHostAddress() + ": " + e.getMessage());
            }
            return new PingResult(address.getHostAddress(), hostname, state);
        }
    } // end inner class Ping

    public class PingResult {
        private String ipAddress;
        private String hostname;
        private boolean state;

        public PingResult(String ipAddress, String hostname, boolean state) {
            this.ipAddress = ipAddress;
            this.state = state;
            this.hostname = hostname;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getHostname() {
            return hostname;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public boolean isState() {
            return state;
        }

        public void setState(boolean state) {
            this.state = state;
        }
    }
}
