package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.SystemSetting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SystemSettingDAO manages file-based platform settings.
 *
 * Storage file:
 * system_settings.txt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class SystemSettingDAO {

    private static final String FILE_NAME = "system_settings.txt";

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
        List<SystemSetting> settings = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            SystemSetting setting = SystemSetting.fromFileString(line);

            if (setting != null && !setting.getSettingKey().isEmpty()) {
                settings.add(setting);
            }
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

        Map<String, String> settings = getSettingsMap(context);

        if (!settings.containsKey(cleanKey)) {
            return defaultValue;
        }

        String value = settings.get(cleanKey);

        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
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
        List<String> lines = new ArrayList<>();

        if (settings != null) {
            for (SystemSetting setting : settings) {
                if (setting != null && !setting.getSettingKey().isEmpty()) {
                    lines.add(setting.toFileString());
                }
            }
        }

        return FileUtil.writeLines(context, FILE_NAME, lines);
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

    private void ensureDefaultValues(Map<String, String> map) {
        if (map == null) {
            return;
        }

        putIfMissing(map, KEY_APP_NAME, "NextExamLK");
        putIfMissing(map, KEY_INSTITUTION_NAME, "Sri Lanka Institute of Information Technology");
        putIfMissing(map, KEY_ACADEMIC_YEAR, "2026");
        putIfMissing(map, KEY_SEMESTER, "Year 1 Semester 2");
        putIfMissing(map, KEY_SUPPORT_EMAIL, "support@nextexam.lk");
        putIfMissing(map, KEY_SUPPORT_PHONE, "+94 77 000 0000");
        putIfMissing(map, KEY_FOOTER_TEXT, "Secure Online Examination and Result Management Platform");
        putIfMissing(map, KEY_SYSTEM_STATUS, "Online");
        putIfMissing(map, KEY_DEFAULT_EXAM_NOTE, "Please read all exam rules carefully before starting the examination.");
        putIfMissing(map, KEY_HELP_DESK_MESSAGE, "Contact the academic support team if you face login, exam, result, or document issues.");
    }

    private void putIfMissing(Map<String, String> map, String key, String value) {
        if (!map.containsKey(key) || map.get(key) == null || map.get(key).trim().isEmpty()) {
            map.put(key, value);
        }
    }
}