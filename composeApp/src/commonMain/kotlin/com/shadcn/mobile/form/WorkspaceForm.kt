package com.shadcn.mobile.form

/** Local demo values. Notification preferences are edited in the sheet. */
data class FormValues(
    val name: String = "",
    val email: String = "",
    val roleId: String = "",
    val acceptedTerms: Boolean = false,
    val emailUpdates: Boolean = true,
    val productNews: Boolean = false,
)

data class FormErrors(
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val terms: String? = null,
) {
    val isValid: Boolean
        get() = name == null && email == null && role == null && terms == null
}

data class RoleOption(
    val id: String,
    val title: String,
    val description: String,
)

val Roles: List<RoleOption> = listOf(
    RoleOption("designer", "设计师", "Designer · 设计体验与界面"),
    RoleOption("developer", "开发者", "Developer · 构建产品与功能"),
    RoleOption("product", "产品经理", "Product · 规划产品与协作"),
    RoleOption("other", "其他", "Other · 探索更多可能"),
)

/** Keep user-entered text while editing; normalize only for validation or submission. */
fun FormValues.normalized(): FormValues = copy(
    name = name.trim(),
    email = email.trim(),
    roleId = roleId.trim(),
)

fun validateForm(values: FormValues): FormErrors {
    val normalized = values.normalized()
    return FormErrors(
        name = when {
            normalized.name.isEmpty() -> "请输入工作空间名称"
            normalized.name.length !in 2..40 -> "名称需要 2–40 个字符"
            else -> null
        },
        email = when {
            normalized.email.isEmpty() -> "请输入工作邮箱"
            !isValidEmail(normalized.email) -> "请输入有效的邮箱地址"
            else -> null
        },
        role = if (Roles.none { it.id == normalized.roleId }) "请选择你的角色" else null,
        terms = if (!normalized.acceptedTerms) "请先阅读并同意演示条款" else null,
    )
}

/** Matches either language and returns an empty list for an unmatched query. */
fun filterRoles(query: String): List<RoleOption> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return Roles
    return Roles.filter { role ->
        role.id.contains(normalizedQuery, ignoreCase = true) ||
            role.title.contains(normalizedQuery, ignoreCase = true) ||
            role.description.contains(normalizedQuery, ignoreCase = true)
    }
}

private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/** A practical input check, not a claim that the address exists or can receive mail. */
private fun isValidEmail(email: String): Boolean {
    if (!emailPattern.matches(email)) return false
    val localPart = email.substringBefore('@')
    val domain = email.substringAfter('@')
    return !localPart.startsWith('.') && !localPart.endsWith('.') &&
        !localPart.contains("..") && domain.split('.').all { it.isNotEmpty() }
}
