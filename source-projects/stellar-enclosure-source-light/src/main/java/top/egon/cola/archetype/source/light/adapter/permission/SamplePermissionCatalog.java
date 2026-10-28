package top.egon.cola.archetype.source.light.adapter.permission;

import top.egon.cola.platform.tianquan.jianshen.contract.permission.annotation.LocalizedText;
import top.egon.cola.platform.tianquan.jianshen.contract.permission.annotation.PermissionDefinition;
import top.egon.cola.platform.tianquan.jianshen.contract.permission.annotation.PermissionMessage;

@PermissionDefinition(code = "stellar-enclosure-source-light:course:create", meaning = "Allow course create", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.course.create")
@PermissionDefinition(code = "stellar-enclosure-source-light:course:read", meaning = "Allow course read", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.course.read")
@PermissionDefinition(code = "stellar-enclosure-source-light:permission:grant", meaning = "Allow permission grant", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.permission.grant")
@PermissionDefinition(code = "stellar-enclosure-source-light:permission:read", meaning = "Allow permission read", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.permission.read")
@PermissionDefinition(code = "stellar-enclosure-source-light:role:assign", meaning = "Allow role assign", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.role.assign")
@PermissionDefinition(code = "stellar-enclosure-source-light:school-class:create", meaning = "Allow school-class create", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.school-class.create")
@PermissionDefinition(code = "stellar-enclosure-source-light:school-class:read", meaning = "Allow school-class read", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.school-class.read")
@PermissionDefinition(code = "stellar-enclosure-source-light:school-class:schedule-course", meaning = "Allow school-class schedule-course", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.school-class.schedule-course")
@PermissionDefinition(code = "stellar-enclosure-source-light:user:create", meaning = "Allow user create", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.user.create")
@PermissionDefinition(code = "stellar-enclosure-source-light:user:read", meaning = "Allow user read", labelPage = "stellar-enclosure-source-light.permissions", labelKey = "permission.user.read")
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.course.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Create course"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.course.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Read course"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.permission.grant", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Grant permission"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.permission.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Read user permissions"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.role.assign", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Assign role"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.school-class.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Create school class"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.school-class.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Read school class"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.school-class.schedule-course", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Schedule course"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.user.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Create user"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "api.user.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Read user"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.course.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow course create"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.course.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow course read"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.permission.grant", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow permission grant"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.permission.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow permission read"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.role.assign", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow role assign"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.school-class.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow school-class create"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.school-class.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow school-class read"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.school-class.schedule-course", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow school-class schedule-course"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.user.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow user create"))
@PermissionMessage(pageCode = "stellar-enclosure-source-light.permissions", key = "permission.user.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow user read"))
public final class SamplePermissionCatalog {
    private SamplePermissionCatalog() { }
}
