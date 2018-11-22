package datamodel;

// Singleton class to create hash map of scripts that correspond to button names on the enumeration tab

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WmiScripts {
    // instance vars
    Map<String, String> scripts;

    // constructor
    public WmiScripts() {
        this.scripts = new HashMap<>();
        addScripts();
    }

    // methods
    public void addScripts() {
        scripts.put("Running Programs", "Get-CimInstance -Query \'SELECT * from Win32_Process\' | Select-Object ProcessID, Name");
//        scripts.put("Running Programs", "Get-CimInstance -Query \'SELECT * from Win32_Process\' ");
//        scripts.put("Running Programs", "Get-Process | Select-Object Id, ProcessName");
//        scripts.put("Running Programs", "Get-Process");  // works
    }

    // getters and setters
    public String getScript(String scriptName) {  // returns script from hash map to caller
        return scripts.get(scriptName);
    }

    public String getScript(String scriptName, String hostName) {  // returns script from hash map to caller
        String[] computerName = hostName.split("\\.",2);
        StringBuilder sb = new StringBuilder(scripts.get(scriptName));
        String host = "-ComputerName " + computerName[0] + " ";
        sb.insert(16, host);
        return sb.toString();
    }



//    public static void main(String[] args) {
//        WmiScripts wmi = new WmiScripts();
//        System.out.println(wmi.getScript("Running Programs"));
//    }
}
