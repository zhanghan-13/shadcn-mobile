package com.shadcn.mobile.form

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkspaceFormTest {
    private val validValues = FormValues(
        name = "设计工作室",
        email = "hello+design@example.com",
        roleId = "designer",
        acceptedTerms = true,
    )

    @Test
    fun emptyFormShowsAllRequiredErrors() {
        val errors = validateForm(FormValues())

        assertFalse(errors.isValid)
        assertNotNull(errors.name)
        assertNotNull(errors.email)
        assertNotNull(errors.role)
        assertNotNull(errors.terms)
    }

    @Test
    fun validChineseNameAndWorkEmailPass() {
        assertTrue(validateForm(validValues).isValid)
    }

    @Test
    fun invalidEmailsFailWithoutChangingOtherFields() {
        listOf(
            "hello",
            "hello@",
            "@example.com",
            "hello@example",
            "hello world@example.com",
            "hello@@example.com",
            "hello@example..com",
            ".hello@example.com",
            "hello.@example.com",
        ).forEach { email ->
            val errors = validateForm(validValues.copy(email = email))

            assertNotNull(errors.email, "Expected an email error for $email")
            assertNull(errors.name)
            assertNull(errors.role)
            assertNull(errors.terms)
        }
    }

    @Test
    fun termsMustBeAccepted() {
        val errors = validateForm(validValues.copy(acceptedTerms = false))

        assertNotNull(errors.terms)
        assertFalse(errors.isValid)
    }

    @Test
    fun roleMustComeFromAvailableOptions() {
        assertNotNull(validateForm(validValues.copy(roleId = "admin")).role)
        Roles.forEach { role ->
            assertNull(validateForm(validValues.copy(roleId = role.id)).role)
        }
    }

    @Test
    fun trimsOuterWhitespaceBeforeValidation() {
        val values = validValues.copy(
            name = "  设计工作室  ",
            email = "  hello+design@example.com \n",
            roleId = " designer ",
        )

        assertTrue(validateForm(values).isValid)
        assertEquals(validValues, values.normalized())
        assertEquals("  设计工作室  ", values.name)
        assertNotNull(validateForm(validValues.copy(name = " \n ")).name)
    }

    @Test
    fun nameLengthHasInclusiveBounds() {
        assertNotNull(validateForm(validValues.copy(name = "甲")).name)
        assertNull(validateForm(validValues.copy(name = "甲乙")).name)
        assertNull(validateForm(validValues.copy(name = "甲".repeat(40))).name)
        assertNotNull(validateForm(validValues.copy(name = "甲".repeat(41))).name)
    }

    @Test
    fun roleSearchSupportsChineseAndCaseInsensitiveEnglish() {
        assertEquals(listOf("designer"), filterRoles("设计师").map { it.id })
        assertEquals(listOf("developer"), filterRoles("  DEVELOPER  ").map { it.id })
        assertEquals(listOf("product"), filterRoles("Product").map { it.id })
        assertEquals(listOf("other"), filterRoles("其他").map { it.id })
    }

    @Test
    fun roleSearchHandlesEmptyQueriesAndNoResults() {
        assertEquals(Roles, filterRoles(""))
        assertEquals(Roles, filterRoles("   "))
        assertTrue(filterRoles("astronaut").isEmpty())
    }

    @Test
    fun notificationDefaultsAreOptInForUpdatesOnly() {
        assertTrue(FormValues().emailUpdates)
        assertFalse(FormValues().productNews)
        assertTrue(validateForm(validValues.copy(emailUpdates = false, productNews = true)).isValid)
    }
}
