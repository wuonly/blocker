package com.merxury.blocker.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileUtilsTest {

    @Test
    fun test1() {
        assertTrue(FileUtils.isExist("/data/system/ifw/gib.xml"))
        assertFalse(FileUtils.isExist("/data/ifw/gib1.xml"))
    }
}