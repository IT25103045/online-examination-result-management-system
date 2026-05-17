package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * SystemSetting stores one platform-level setting.
 *
 * File format:
 * settingKey|settingValue
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class SystemSetting {

    private String settingKey;
    private String settingValue;

    public SystemSetting() {
    }

    public SystemSetting(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public String getSettingKey() {
        return safe(settingKey);
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return safe(settingValue);
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String toFileString() {
        return FileUtil.clean(getSettingKey()) + "|" + FileUtil.clean(getSettingValue());
    }

    public static SystemSetting fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 2) {
            return null;
        }

        return new SystemSetting(data[0], data[1]);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}