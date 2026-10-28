package top.egon.cola.archetype.source.web.adapter.permission;

import top.egon.cola.platform.tianquan.jianshen.contract.permission.annotation.LocalizedText;
import top.egon.cola.platform.tianquan.jianshen.contract.permission.annotation.PermissionDefinition;
import top.egon.cola.platform.tianquan.jianshen.contract.permission.annotation.PermissionMessage;

@PermissionDefinition(code = "stellar-enclosure-source-web:grade:create", meaning = "Allow grade create", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.grade.create")
@PermissionDefinition(code = "stellar-enclosure-source-web:grade:read", meaning = "Allow grade read", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.grade.read")
@PermissionDefinition(code = "stellar-enclosure-source-web:permission:grant", meaning = "Allow permission grant", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.permission.grant")
@PermissionDefinition(code = "stellar-enclosure-source-web:permission:read", meaning = "Allow permission read", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.permission.read")
@PermissionDefinition(code = "stellar-enclosure-source-web:role:assign", meaning = "Allow role assign", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.role.assign")
@PermissionDefinition(code = "stellar-enclosure-source-web:school-class:assign-user", meaning = "Allow school-class assign-user", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.school-class.assign-user")
@PermissionDefinition(code = "stellar-enclosure-source-web:school-class:create", meaning = "Allow school-class create", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.school-class.create")
@PermissionDefinition(code = "stellar-enclosure-source-web:school-class:read", meaning = "Allow school-class read", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.school-class.read")
@PermissionDefinition(code = "stellar-enclosure-source-web:user:create", meaning = "Allow user create", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.user.create")
@PermissionDefinition(code = "stellar-enclosure-source-web:user:read", meaning = "Allow user read", labelPage = "stellar-enclosure-source-web.permissions", labelKey = "permission.user.read")
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.grade.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Create"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.grade.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Get"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.permission.grant", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Grant"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.permission.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Get permission tree"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.role.assign", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Assign"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.school-class.assign-user", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Assign user"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.school-class.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Create"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.school-class.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Get"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.user.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Create"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "api.user.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Get"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.grade.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow grade create"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.grade.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow grade read"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.permission.grant", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow permission grant"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.permission.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow permission read"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.role.assign", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow role assign"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.school-class.assign-user", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow school-class assign-user"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.school-class.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow school-class create"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.school-class.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow school-class read"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.user.create", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow user create"))
@PermissionMessage(pageCode = "stellar-enclosure-source-web.permissions", key = "permission.user.read", defaultLocale = "en-US", defaults = @LocalizedText(locale = "en-US", text = "Allow user read"))
public final class SamplePermissionCatalog {
    private SamplePermissionCatalog() { }
}
