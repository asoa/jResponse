package datamodel;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertLoggedOnUser {
    // instance vars
    private Connection conn;
    private Map<String, List<String>> wmiResults;

    // constructor
    public InsertLoggedOnUser(Connection conn, Map<String, List<String>> wmiResults) {
        this.conn = conn;
        this.wmiResults = wmiResults;
        insertDB(wmiResults);
    }

    // methods
    public boolean insertDB(Map<String, List<String>> wmiResults) {
        String host;
        int recordCount = 0;
        String strFormat =
                "IF EXISTS (SELECT 1 FROM currentUser WHERE hostName='%s' AND userName='%s')\n" +
                        "BEGIN\n" +
                            "UPDATE currentUser\n" +
                            "SET userName='%s'\n" +
                            "WHERE hostName='%s'\n" +
                        "END\n" +
                "ELSE\n" +
                        "BEGIN\n" +
                        "INSERT into currentUser (hostName,userName) VALUES ('%s','%s')\n" +
                        "END\n";

        for(Map.Entry<String, List<String>> entry: wmiResults.entrySet()) {
            host = entry.getKey(); // get hostname
//            sublist = entry.getValue().subList(2,7);
            String user = getMatch(entry.getValue().get(3));
            try(Statement statement = conn.createStatement()) {
//                List<String> values = new ArrayList<>(getMatch(sublist));
                String hostName = (host).toLowerCase();
                String insertSQL = String.format(strFormat,hostName,user,user,hostName,hostName,user);
                statement.execute(insertSQL);
                recordCount++;
//                System.out.println(s);

            } catch (Exception e) {
                System.out.println("Error in insertDB computerInfo " + e);
            }
        }
        System.out.printf("Inserted %d records into the currentUser table\n", recordCount);
        return true;
    }

    public String getMatch(String s) {
        Pattern patternObj = Pattern.compile("(\\w+)\\s+");
        Matcher match = patternObj.matcher(s);
        if (match.find()) {
            return(match.group(1).toLowerCase());
        }
        else return "";
    }
}
