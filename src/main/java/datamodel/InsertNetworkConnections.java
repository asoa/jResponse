package datamodel;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertNetworkConnections {
    // instance vars
    private Connection conn;
    private Map<String, List<String>> wmiResults;

    // constructor
    public InsertNetworkConnections(Connection conn, Map<String, List<String>> wmiResults) {
        this.conn = conn;
        this.wmiResults = wmiResults;
        insertDB(wmiResults);
    }

    public boolean insertDB(Map<String, List<String>> wmiResults) {
        String host;
        List<String> sublist;
        int recordCount = 0;
        String strFormat =
                "IF NOT EXISTS\n" +
                        "(SELECT 1 FROM networkConnections WHERE localPort = '%s' AND remotePort = '%s')\n" +
                        "BEGIN\n" +
                        "INSERT INTO dbo.networkConnections (hostName,localAddress,localPort,remoteAddress,remotePort,owningProcess) VALUES ('%s','%s','%s','%s','%s','%s')\n" +
                        "END\n";
        for(Map.Entry<String, List<String>> entry: wmiResults.entrySet()) {
            host = entry.getKey(); // get hostname
            sublist = entry.getValue().subList(3,entry.getValue().size()-2);
            try(Statement statement = conn.createStatement()) {
                for(String s: sublist) {
                    try {
                        List<String> values = new ArrayList<>(getMatch(s));
                        if(values.size() == 5) {
                            String insertSQL = String.format(strFormat,values.get(1),values.get(3),host,values.get(0),values.get(1),values.get(2),values.get(3),values.get(4));
                            statement.execute(insertSQL);
                            recordCount++;
                        } else {
                            continue;
                        }

                    } catch (Exception e) {
                        System.out.println((char)27 + "[31m" + e);
                    }
                }
            } catch (Exception e) {
                System.out.println((char)27 + "[31m" + "Error in insertDB networkConnections " + e);
            }
        }
//        System.out.printf("Inserted %d records into the networkConnections table\n", recordCount);
        System.out.println((char)27 + "[32m" + "\nInserted " + recordCount + " records into the networkConnections table");
        return true;
    }

    public List<String> getMatch(String line) {
        List<String> matches = new ArrayList<>();
        Pattern patternObj = Pattern.compile("(\\d+.\\d+.\\d+.\\d+)\\s+(\\d+)\\s+(\\d+.\\d+.\\d+.\\d+)\\s+(\\d+)\\s+(\\d+)");

        try {
            Matcher match = patternObj.matcher(line);
            if(match.find()) {
                matches.add(match.group(1));
                matches.add(match.group(2));
                matches.add(match.group(3));
                matches.add(match.group(4));
                matches.add(match.group(5));
            }
        } catch (Exception e) {
            System.out.println((char)27 + "[33m" + "Error in InsertNetworkConnections::getMatch() " + e);
        }
        return matches;
    }
}
