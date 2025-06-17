import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbHelper {
    private static final String DB_PATH = "assets/BC220206416.accdb";
    private static final String URL = "jdbc:ucanaccess://" + DB_PATH;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void insertData(int temp, int sale) throws SQLException {
        String query = "INSERT INTO salesdata (temprature, sale) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, temp);
            ps.setInt(2, sale);
            ps.executeUpdate();
        }
    }

    public static List<DataPoint> getAllData() throws SQLException {
        List<DataPoint> list = new ArrayList<>();
        String query = "SELECT * FROM salesdata ORDER BY temprature ASC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new DataPoint(rs.getInt("id"), rs.getInt("temprature"), rs.getInt("sale")));
            }
        }
        return list;
    }

    public static void deleteAll() throws SQLException {
        String query = "DELETE FROM salesdata";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        }
    }
}
