package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.SystemSetting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SystemSettingDAO manages MySQL platform settings.
 *
 * MySQL table:
 * system_settings
 *
 * Columns:
 * setting_key, setting_value
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class SystemSettingDAO {

    public static final String KEY_APP_NAME = "appName";
    public static final String KEY_INSTITUTION_NAME = "institutionName";
    public static final String KEY_ACADEMIC_YEAR = "academicYear";
    public static final String KEY_SEMESTER = "semester";
    public static final String KEY_SUPPORT_EMAIL = "supportEmail";
    public static final String KEY_SUPPORT_PHONE = "supportPhone";
    public static final String KEY_FOOTER_TEXT = "footerText";
    public static final String KEY_SYSTEM_STATUS = "systemStatus";
    public static final String KEY_DEFAULT_EXAM_NOTE = "defaultExamNote";
    public static final String KEY_HELP_DESK_MESSAGE = "helpDeskMessage";

    public List<SystemSetting> getAllSettings(ServletContext context) {
        ensureDefaultSettings();

        List<SystemSetting> settings = new ArrayList<>();

        String sql = "SELECT setting_key, setting_value " +
                "FROM system_settings " +
                "ORDER BY FIELD(setting_key, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?), setting_key ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, KEY_APP_NAME);
            statement.setString(2, KEY_INSTITUTION_NAME);
            statement.setString(3, KEY_ACADEMIC_YEAR);
            statement.setString(4, KEY_SEMESTER);
            statement.setString(5, KEY_SUPPORT_EMAIL);
            statement.setString(6, KEY_SUPPORT_PHONE);
            statement.setString(7, KEY_FOOTER_TEXT);
            statement.setString(8, KEY_SYSTEM_STATUS);
            statement.setString(9, KEY_DEFAULT_EXAM_NOTE);
            statement.setString(10, KEY_HELP_DESK_MESSAGE);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    settings.add(mapResultSetToSystemSetting(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("SYSTEMSETTINGDAO ERROR -> getAllSettings failed");
            e.printStackTrace();
        }

        if (settings.isEmpty()) {
            settings = getDefaultSettings();
            saveAllSettings(context, settings);
        }

        return settings;
    }

    public Map<String, String> getSettingsMap(ServletContext context) {
        Map<String, String> map = new LinkedHashMap<>();

        for (SystemSetting setting : getAllSettings(context)) {
            map.put(setting.getSettingKey(), setting.getSettingValue());
        }

        ensureDefaultValues(map);
        return map;
    }

    public String getValue(ServletContext context, String key) {
        return getValue(context, key, "");
    }

    public String getValue(ServletContext context, String key, String defaultValue) {
        String cleanKey = FileUtil.clean(key);

        if (cleanKey.isEmpty()) {
            return defaultValue;
        }

        String sql = "SELECT setting_value FROM system_settings " +
                "WHERE setting_key = ? " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanKey);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String value = safe(resultSet.getString("setting_value"));
                    return value.isEmpty() ? defaultValue : value;
                }
            }

        } catch (SQLException e) {
            System.out.println("SYSTEMSETTINGDAO ERROR -> getValue failed for " + cleanKey);
            e.printStackTrace();
        }

        Map<String, String> defaults = defaultSettingsMap();

        if (defaults.containsKey(cleanKey)) {
            return defaults.get(cleanKey);
        }

        return defaultValue;
    }

    public boolean updateSettings(ServletContext context, Map<String, String> updatedSettings) {
        Map<String, String> map = getSettingsMap(context);

        if (updatedSettings != null) {
            for (Map.Entry<String, String> entry : updatedSettings.entrySet()) {
                String key = FileUtil.clean(entry.getKey());
                String value = FileUtil.clean(entry.getValue());

                if (!key.isEmpty()) {
                    map.put(key, value);
                }
            }
        }

        ensureDefaultValues(map);

        List<SystemSetting> settings = new ArrayList<>();

        settings.add(new SystemSetting(KEY_APP_NAME, map.get(KEY_APP_NAME)));
        settings.add(new SystemSetting(KEY_INSTITUTION_NAME, map.get(KEY_INSTITUTION_NAME)));
        settings.add(new SystemSetting(KEY_ACADEMIC_YEAR, map.get(KEY_ACADEMIC_YEAR)));
        settings.add(new SystemSetting(KEY_SEMESTER, map.get(KEY_SEMESTER)));
        settings.add(new SystemSetting(KEY_SUPPORT_EMAIL, map.get(KEY_SUPPORT_EMAIL)));
        settings.add(new SystemSetting(KEY_SUPPORT_PHONE, map.get(KEY_SUPPORT_PHONE)));
        settings.add(new SystemSetting(KEY_FOOTER_TEXT, map.get(KEY_FOOTER_TEXT)));
        settings.add(new SystemSetting(KEY_SYSTEM_STATUS, map.get(KEY_SYSTEM_STATUS)));
        settings.add(new SystemSetting(KEY_DEFAULT_EXAM_NOTE, map.get(KEY_DEFAULT_EXAM_NOTE)));
        settings.add(new SystemSetting(KEY_HELP_DESK_MESSAGE, map.get(KEY_HELP_DESK_MESSAGE)));

        return saveAllSettings(context, settings);
    }

    public boolean saveAllSettings(ServletContext context, List<SystemSetting> settings) {
        if (settings == null) {
            settings = getDefaultSettings();
        }

        String sql = "INSERT INTO system_settings (setting_key, setting_value) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (SystemSetting setting : settings) {
                if (setting != null && !setting.getSettingKey().isEmpty()) {
                    statement.setString(1, setting.getSettingKey());
                    statement.setString(2, setting.getSettingValue());
                    statement.addBatch();
                }
            }

            statement.executeBatch();
            return true;

        } catch (SQLException e) {
            System.out.println("SYSTEMSETTINGDAO ERROR -> saveAllSettings failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateValue(ServletContext context, String key, String value) {
        String cleanKey = FileUtil.clean(key);

        if (cleanKey.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO system_settings (setting_key, setting_value) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanKey);
            statement.setString(2, FileUtil.clean(value));

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("SYSTEMSETTINGDAO ERROR -> updateValue failed for " + cleanKey);
            e.printStackTrace();
            return false;
        }
    }

    public List<SystemSetting> getDefaultSettings() {
        List<SystemSetting> defaults = new ArrayList<>();

        defaults.add(new SystemSetting(KEY_APP_NAME, "NextExamLK"));
        defaults.add(new SystemSetting(KEY_INSTITUTION_NAME, "Sri Lanka Institute of Information Technology"));
        defaults.add(new SystemSetting(KEY_ACADEMIC_YEAR, "2026"));
        defaults.add(new SystemSetting(KEY_SEMESTER, "Year 1 Semester 2"));
        defaults.add(new SystemSetting(KEY_SUPPORT_EMAIL, "support@nextexam.lk"));
        defaults.add(new SystemSetting(KEY_SUPPORT_PHONE, "+94 77 000 0000"));
        defaults.add(new SystemSetting(KEY_FOOTER_TEXT, "Secure Online Examination and Result Management Platform"));
        defaults.add(new SystemSetting(KEY_SYSTEM_STATUS, "Online"));
        defaults.add(new SystemSetting(KEY_DEFAULT_EXAM_NOTE, "Please read all exam rules carefully before starting the examination."));
        defaults.add(new SystemSetting(KEY_HELP_DESK_MESSAGE, "Contact the academic support team if you face login, exam, result, or document issues."));

        return defaults;
    }

    private void ensureDefaultSettings() {
        saveAllSettings(null, getDefaultSettings());
    }

    private void ensureDefaultValues(Map<String, String> map) {
        if (map == null) {
            return;
        }

        Map<String, String> defaults = defaultSettingsMap();

        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            putIfMissing(map, entry.getKey(), entry.getValue());
        }
    }

    private Map<String, String> defaultSettingsMap() {
        Map<String, String> defaults = new LinkedHashMap<>();

        defaults.put(KEY_APP_NAME, "NextExamLK");
        defaults.put(KEY_INSTITUTION_NAME, "Sri Lanka Institute of Information Technology");
        defaults.put(KEY_ACADEMIC_YEAR, "2026");
        defaults.put(KEY_SEMESTER, "Year 1 Semester 2");
        defaults.put(KEY_SUPPORT_EMAIL, "support@nextexam.lk");
        defaults.put(KEY_SUPPORT_PHONE, "+94 77 000 0000");
        defaults.put(KEY_FOOTER_TEXT, "Secure Online Examination and Result Management Platform");
        defaults.put(KEY_SYSTEM_STATUS, "Online");
        defaults.put(KEY_DEFAULT_EXAM_NOTE, "Please read all exam rules carefully before starting the examination.");
        defaults.put(KEY_HELP_DESK_MESSAGE, "Contact the academic support team if you face login, exam, result, or document issues.");

        return defaults;
    }

    private void putIfMissing(Map<String, String> map, String key, String value) {
        if (!map.containsKey(key) || map.get(key) == null || map.get(key).trim().isEmpty()) {
            map.put(key, value);
        }
    }

    private SystemSetting mapResultSetToSystemSetting(ResultSet resultSet) throws SQLException {
        return new SystemSetting(
                safe(resultSet.getString("setting_key")),
                safe(resultSet.getString("setting_value"))
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}