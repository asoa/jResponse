package datamodel;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertProcess {
    // instance vars
    private Connection conn;
    private Map<String, List<String>> wmiResults;

    // constructor
    public InsertProcess(Connection conn,Map<String, List<String>> wmiResults) {
        this.conn = conn;
        this.wmiResults = wmiResults;
        insertDB(wmiResults);
    }

    // methods
    public boolean insertDB(Map<String, List<String>> wmiResults) {
        String host;
        List<String> sublist;
        int recordCount = 0;
        String strFormat =
                "IF NOT EXISTS\n" +
                        "(SELECT 1 FROM processLog WHERE processId = '%s')\n" +
                        "BEGIN\n" +
                        "INSERT INTO dbo.processLog (hostName, processId, processName) VALUES ('%s','%s','%s')\n" +
                        "END\n";
        for(Map.Entry<String, List<String>> entry: wmiResults.entrySet()) {
            host = entry.getKey(); // get hostname
            sublist = entry.getValue().subList(3, entry.getValue().size());  // get list of processes
            try (Statement statement = conn.createStatement()){
                for(String s: sublist) {  // iterate over processes in sublist
                    try {
                        List<String> pid_process = getMatch(s);
                        if (pid_process.size() == 0) {
                            continue;
                        } else {
                            String insertSQL = String.format(strFormat, pid_process.get(0), host, pid_process.get(0), pid_process.get(1));
                            statement.execute(insertSQL);
//                        System.out.printf("Host: %s, ProcessID:%s, ProcesName:%s\n", host, pid_process.get(0), pid_process.get(1));
                            recordCount++;
                        }
                    } catch (Exception e) {
                        System.out.println("Error in getMatch(): " + e);
                        continue;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in dbProcessInsert() " + e);
                return false;
            }
        }
        System.out.printf("Inserted %d records into the process table\n", recordCount);
        return true;
    }

    public List<String> getMatch(String s) {
        List<String> matches = new ArrayList<>();

        Pattern patternObj = Pattern.compile("(\\d+)\\s+(\\w+.exe)");
        Matcher match = patternObj.matcher(s);

        if(match.find()) {
            String pid = match.group(1);
            String process = match.group(2);
            matches.add(pid);
            matches.add(process);
        }
        return matches;
    }
}
