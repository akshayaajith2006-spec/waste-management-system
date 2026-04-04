import java.sql.*;
import java.util.*;

public class DatabaseManager {

  /* Oracle connection config */
  private static final String URL      = "jdbc:oracle:thin:@localhost:1521:xe";
  private static final String USERNAME = "system";
  private static final String PASSWORD = "1234";

  /* Get DB connection */
  public static Connection getConnection() throws SQLException {
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver");
    } catch (ClassNotFoundException e) {
      throw new SQLException("Oracle JDBC Driver not found. Add ojdbc.jar to classpath.", e);
    }
    return DriverManager.getConnection(URL, USERNAME, PASSWORD);
  }

  /* INSERT: Save a new request */
  public static boolean insertRequest(String id, String name, String phone,
      String address, String wasteType, String date, String submittedAt) {

    String sql = "INSERT INTO REQUESTS (ID, NAME, PHONE, ADDRESS, WASTE_TYPE, PICKUP_DATE, STATUS, SUBMITTED_AT) "
               + "VALUES (?, ?, ?, ?, ?, TO_DATE(?,'YYYY-MM-DD'), 'Pending', TO_DATE(?,'YYYY-MM-DD'))";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, id);
      ps.setString(2, name);
      ps.setString(3, phone);
      ps.setString(4, address);
      ps.setString(5, wasteType);
      ps.setString(6, date);
      ps.setString(7, submittedAt);

      int rows = ps.executeUpdate();
      System.out.println("✅ Request inserted: " + id);
      return rows > 0;

    } catch (SQLException e) {
      System.err.println("❌ Insert failed: " + e.getMessage());
      return false;
    }
  }

  /* SELECT: Get request by ID */
  public static Map<String, String> getRequestById(String id) {
    String sql = "SELECT * FROM REQUESTS WHERE ID = ?";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, id.toUpperCase().trim());
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id",          rs.getString("ID"));
        row.put("name",        rs.getString("NAME"));
        row.put("phone",       rs.getString("PHONE"));
        row.put("address",     rs.getString("ADDRESS"));
        row.put("wasteType",   rs.getString("WASTE_TYPE"));
        row.put("pickupDate",  rs.getString("PICKUP_DATE"));
        row.put("status",      rs.getString("STATUS"));
        row.put("submittedAt", rs.getString("SUBMITTED_AT"));
        return row;
      }

    } catch (SQLException e) {
      System.err.println("❌ Fetch failed: " + e.getMessage());
    }
    return null;
  }

  /* SELECT: Get all requests */
  public static List<Map<String, String>> getAllRequests() {
    String sql = "SELECT * FROM REQUESTS ORDER BY SUBMITTED_AT DESC";
    List<Map<String, String>> list = new ArrayList<>();

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("id",          rs.getString("ID"));
        row.put("name",        rs.getString("NAME"));
        row.put("phone",       rs.getString("PHONE"));
        row.put("address",     rs.getString("ADDRESS"));
        row.put("wasteType",   rs.getString("WASTE_TYPE"));
        row.put("pickupDate",  rs.getString("PICKUP_DATE"));
        row.put("status",      rs.getString("STATUS"));
        row.put("submittedAt", rs.getString("SUBMITTED_AT"));
        list.add(row);
      }

    } catch (SQLException e) {
      System.err.println("❌ GetAll failed: " + e.getMessage());
    }
    return list;
  }

  /* UPDATE: Change request status */
  public static boolean updateStatus(String id, String newStatus) {
    List<String> allowed = Arrays.asList("Pending", "In Progress", "Done");
    if (!allowed.contains(newStatus)) {
      System.err.println("❌ Invalid status: " + newStatus);
      return false;
    }

    String sql = "UPDATE REQUESTS SET STATUS = ? WHERE ID = ?";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, newStatus);
      ps.setString(2, id);
      int rows = ps.executeUpdate();
      System.out.println("✅ Status updated → " + id + " = " + newStatus);
      return rows > 0;

    } catch (SQLException e) {
      System.err.println("❌ Update failed: " + e.getMessage());
      return false;
    }
  }

  /* SELECT: Count by status */
  public static Map<String, Integer> getStatusCounts() {
    String sql = "SELECT STATUS, COUNT(*) AS CNT FROM REQUESTS GROUP BY STATUS";
    Map<String, Integer> counts = new HashMap<>();
    counts.put("Pending",     0);
    counts.put("In Progress", 0);
    counts.put("Done",        0);

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        counts.put(rs.getString("STATUS"), rs.getInt("CNT"));
      }

    } catch (SQLException e) {
      System.err.println("❌ Count failed: " + e.getMessage());
    }
    return counts;
  }

  /* INSERT: Register new user */
  public static boolean insertUser(String id, String name, String email,
      String phone, String password) {

    String sql = "INSERT INTO USERS (ID, NAME, EMAIL, PHONE, PASSWORD, JOINED_AT) "
               + "VALUES (?, ?, ?, ?, ?, SYSDATE)";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, id);
      ps.setString(2, name);
      ps.setString(3, email);
      ps.setString(4, phone);
      ps.setString(5, password);
      ps.executeUpdate();
      System.out.println("✅ User registered: " + email);
      return true;

    } catch (SQLException e) {
      System.err.println("❌ User insert failed: " + e.getMessage());
      return false;
    }
  }

  /* SELECT: Login check */
  public static Map<String, String> loginUser(String email, String password) {
    String sql = "SELECT * FROM USERS WHERE EMAIL = ? AND PASSWORD = ?";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, email);
      ps.setString(2, password);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        Map<String, String> user = new LinkedHashMap<>();
        user.put("id",       rs.getString("ID"));
        user.put("name",     rs.getString("NAME"));
        user.put("email",    rs.getString("EMAIL"));
        user.put("phone",    rs.getString("PHONE"));
        user.put("joinedAt", rs.getString("JOINED_AT"));
        return user;
      }

    } catch (SQLException e) {
      System.err.println("❌ Login failed: " + e.getMessage());
    }
    return null;
  }

  /* MAIN: Test connection */
  public static void main(String[] args) {
    System.out.println("=== EcoPickup DB Test ===\n");

    try (Connection con = getConnection()) {
      System.out.println("✅ Oracle Database connected!\n");
    } catch (SQLException e) {
      System.err.println("❌ Cannot connect: " + e.getMessage());
      return;
    }

    System.out.println("--- All Requests ---");
    List<Map<String, String>> all = getAllRequests();
    for (Map<String, String> r : all) {
      System.out.printf("  [%s] %s | %s | %s%n",
          r.get("id"), r.get("name"), r.get("wasteType"), r.get("status"));
    }

    System.out.println("\n--- Status Summary ---");
    Map<String, Integer> counts = getStatusCounts();
    counts.forEach((k, v) -> System.out.println("  " + k + ": " + v));

    System.out.println("\n✅ All tests passed.");
  }
}