package datamodel;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InsertComputerInfo {
    // instance vars
    private Connection conn;
    private Map<String, List<String>> wmiResults;

    // constructor
    public InsertComputerInfo(Connection conn, Map<String, List<String>> wmiResults) {
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
                        "(SELECT 1 FROM computerInfo WHERE hostName = '%s')\n" +
                        "BEGIN\n" +
                        "INSERT INTO dbo.computerInfo (osName, osVersion, servicePackVersion, osArch, hostName) VALUES ('%s','%s','%s','%s','%s')\n" +
                        "END\n";

        for(Map.Entry<String, List<String>> entry: wmiResults.entrySet()) {
            host = entry.getKey(); // get hostname
            sublist = entry.getValue().subList(2,7);
            try(Statement statement = conn.createStatement()) {
                List<String> values = new ArrayList<>(getMatch(sublist));
//                String hostName = (values.get(4) + ".home.mylocal").toLowerCase();
//                String hostName = (values.get(4)).toLowerCase();
                String insertSQL = String.format(strFormat,host,values.get(0),values.get(1),values.get(2),values.get(3),host);
                statement.execute(insertSQL);
                recordCount++;
//                System.out.println(s);

            } catch (Exception e) {
                System.out.println("Error in insertDB computerInfo " + e);
            }
        }
        System.out.printf("Inserted %d records into the computerInfo table\n", recordCount);
        return true;
    }

    public List<String> getMatch(List<String> list) {
        List<String> matches = new ArrayList<>();

        Pattern patternObj = Pattern.compile(":\\s+(.*$)");

        try {
            Iterator<String> it = list.iterator();
            while(it.hasNext()) {
                Matcher match = patternObj.matcher(it.next());
                if(match.find()) {
                    matches.add(match.group(1));
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return matches;
    }


}
