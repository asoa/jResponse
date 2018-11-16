package datamodel;

import java.net.*;
import java.util.Enumeration;

public class Test {
    // instance vars

    // constructor


    // methods 

    // getters and setters

    private void getSubnetMask() {
        InterfaceAddress address = null;
        try {
            InetAddress localHost = Inet4Address.getLocalHost();
//            System.out.println(localHost);
//            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
//            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
//                String subnetMask = String.valueOf(address.getNetworkPrefixLength());
//                System.out.println(address.getNetworkPrefixLength());
//            }
            NetworkInterface networkInterface1 = NetworkInterface.getByInetAddress(localHost);
//            System.out.println(networkInterface1.getInterfaceAddresses().get(1).getNetworkPrefixLength());
            Enumeration<NetworkInterface> i;
            for(i = NetworkInterface.getNetworkInterfaces(); i.hasMoreElements();) {  // iterate over all NICs
                NetworkInterface networkInterface = i.nextElement();  // get interface
                for(InterfaceAddress ifadd: networkInterface.getInterfaceAddresses()) {  // iterate over all ifadd objects on all NICs
                    address = ifadd;
                    System.out.println(address);
                    InetAddress ip = ifadd.getAddress();  // get the ip from the ifadd object
                    if(!ip.isLoopbackAddress() && ip instanceof Inet4Address) {
                        String s_ip = ip.getHostAddress();
                        String b_ip = String.valueOf(ifadd.getBroadcast()).replace("/","");
//                        System.out.println(s_ip);
//                        System.out.println(b_ip);
                    }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void getHostName() {
        try {
            InetAddress addr = InetAddress.getByName("192.168.4.22");
            String hostname = addr.getHostName();
//            String hostname = addr.getCanonicalHostName();
            System.out.println(hostname);
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }

    }

    public void getAllIPs(InetAddress ip) {


//        InetAddress[] ips = InetAddress.getAllByName()
    }

    public static void main(String[] args) {
        Test test = new Test();
//        test.getSubnetMask();
        test.getHostName();
//        SubnetUtils utils = new SubnetUtils("192.168.3.1/24");
    }
}
