/*
TODO: sort aliveHosts using sort() and Comparator
 */

package datamodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.SubnetUtil;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkDiscovery {
    // instance vars
    private List<NetworkInfo> nwInfo = new ArrayList<NetworkInfo>();
    private ObservableList<PingParrallel.PingResult> aliveHosts;
    private List<IP> hostList;

    // constructor
    public NetworkDiscovery() {
        NetworkInfo networkInfo = new NetworkInfo();
        hostList = FXCollections.observableArrayList();
        aliveHosts = FXCollections.observableArrayList();
        networkInfo.getIPList(); // gets the CIDR range for the subnet to be scanned

    }


    // getters and setters
    public List<NetworkInfo> getNwInfo() {
        return nwInfo;
    }

    public List<IP> getHostList() {
        return hostList;
    }

    public ObservableList<PingParrallel.PingResult> getAliveHosts() {
        return aliveHosts;
    }

    public class IP {  // IP inner class start
        // instance vars
        private SimpleStringProperty ip = new SimpleStringProperty("");
        private SimpleStringProperty hostname = new SimpleStringProperty("");

        // constructor
        public IP(String ip) {
            this.ip.set(ip);
        }

        public SimpleStringProperty ipProperty() {
            return ip;
        }

        // methods

        // getters and setters

        public String getIp() {
            return ip.get();
        }

        public void setHostname(String hostname) {
            this.hostname.set(hostname);
        }

        public String getHostname() {
            return hostname.get();
        }

        public SimpleStringProperty hostnameProperty() {
            return hostname;
        }
    } // end of IP inner class

    public class NetworkInfo {  // start of inner class NetworkInfo
        String ipAndCidr;
        String networkAndCidr;
        List<String> nwIPRange;

        public String getIpAndCidr() {
            return ipAndCidr;
        }

        public String getNetworkAndCidr() {
            return networkAndCidr;
        }

        public List<String> getNwIPRange() {
            return nwIPRange;
        }


        // TODO: get all interfaces
        // gets cidr info
        public NetworkInfo() {
            try {
                Enumeration<NetworkInterface> i; // call the static NetworkInterface object that returns an Enumeration instance
                for(i = NetworkInterface.getNetworkInterfaces(); i.hasMoreElements();) {
                    NetworkInterface networkInterface = i.nextElement();  // get next nw interface object
                    for (InterfaceAddress ifadd : networkInterface.getInterfaceAddresses()) { // iterate over all ifadd objects on
                        InetAddress ip = ifadd.getAddress();
                        if(!ip.isLoopbackAddress() && ip instanceof Inet4Address) {
                            try {
                                String broadcast = ifadd.getBroadcast().getHostAddress();
                                ipAndCidr = ifadd.toString().split(" ")[0].substring(1);
                                networkAndCidr = new SubnetUtil(ipAndCidr).getCIDR();
                                nwIPRange = (new SubnetUtil(ipAndCidr).getHostAddressRange());
                                nwInfo.add(this);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }

        }

        public void getIPList() {  // generate list of IPs from cidr
            String nwIP = "";
            Pattern patternObj = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.)");
            Matcher match = patternObj.matcher(networkAndCidr);
            while(match.find()){
                nwIP = match.group(1);
            }
            String firstIP = nwIPRange.get(0);
            String lastIP = nwIPRange.get(1);
            int firstIPlastOctet = Integer.parseInt(firstIP.substring(firstIP.lastIndexOf(".") + 1));
            int lastIPlastOctet = Integer.parseInt(lastIP.substring(lastIP.lastIndexOf(".") + 1));
            int ipRange = (lastIPlastOctet - firstIPlastOctet) + 1;
            for(int i = firstIPlastOctet; i<=lastIPlastOctet; i++) {
                String ip_repr = nwIP + i;
                IP ip = new IP(ip_repr);
//                System.out.println(ip);
                hostList.add(ip);
            }
        }
    } // end of inner class NetworkInfo

//    public static void main(String[] args) {
//        NetworkDiscovery test = new NetworkDiscovery();
//    }
}
