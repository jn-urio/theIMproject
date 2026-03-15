/**
 * Current logged-in user (HR). Set after login, used for applied_by and audit.
 */
public class AppSession {
    private static Integer hrUserId;
    private static String username;
    private static String hrRole;
    private static Integer employeeId;
    private static Integer currentPeriodId;

    public static void setHRUser(int id, String name, String role, Integer empId) {
        hrUserId = id;
        username = name;
        hrRole = role;
        employeeId = empId;
    }

    public static Integer getHrUserId() { return hrUserId; }
    public static String getUsername() { return username; }
    public static String getHrRole() { return hrRole; }
    public static Integer getEmployeeId() { return employeeId; }
    public static boolean isAdmin() { return hrRole != null && hrRole.equalsIgnoreCase("admin"); }

    /** Currently selected payroll period (set from Payroll Periods page). Used to preselect period in dropdowns. */
    public static void setCurrentPeriodId(Integer periodId) { currentPeriodId = periodId; }
    public static Integer getCurrentPeriodId() { return currentPeriodId; }

    public static void clear() { hrUserId = null; username = null; hrRole = null; employeeId = null; currentPeriodId = null; }
}
