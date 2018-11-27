package datamodel;

// Singleton class to create hash map of scripts that correspond to button names on the enumeration tab
import java.util.HashMap;
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
        scripts.put("Computer Info", "Get-CimInstance -Query \'SELECT * FROM Win32_OperatingSystem\' | Select-Object Caption,Version,ServicePackMajorVersion,OSArchitecture,CSName");
        scripts.put("Network Connections", "Invoke-Command -ComputerName {Get-NetTCPConnection -State Established | Select-Object LocalAddress,LocalPort,RemoteAddress,RemotePort,OwningProcess | Format-Table -AutoSize}");
        scripts.put("Logged on User", "Get-CimInstance Win32_ComputerSystem -ComputerName | Select-Object PrimaryOwnerName");
    }

    public String getScript(String scriptName, String hostName) {  // returns script from hash map to caller
        if(scriptName.equals("Network Connections")) {
            String[] computerName = hostName.split("\\.",2);
            StringBuilder sb = new StringBuilder(scripts.get(scriptName));
            String host = computerName[0] + " ";
            sb.insert(29, host);
            return sb.toString();
        } else if (scriptName.equals("Logged on User")) {
            String[] computerName = hostName.split("\\.",2);
            StringBuilder sb = new StringBuilder(scripts.get(scriptName));
            String host = computerName[0] + " ";
            sb.insert(51, host);
            return sb.toString();
        }
        String[] computerName = hostName.split("\\.",2);
        StringBuilder sb = new StringBuilder(scripts.get(scriptName));
        String host = "-ComputerName " + computerName[0] + " ";
        sb.insert(16, host);
        return sb.toString();
    }
}
