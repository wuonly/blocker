package com.merxury.blocker.core.ifw

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.merxury.blocker.core.ApplicationComponents
import com.merxury.blocker.core.IController
import com.merxury.ifw.IntentFirewall
import com.merxury.ifw.IntentFirewallImpl
import com.merxury.ifw.entity.ComponentType

class IfwController(val context: Context) : IController {
    private lateinit var controller: IntentFirewall
    private lateinit var packageInfo: PackageInfo

    override fun switchComponent(packageName: String, componentName: String, state: Int): Boolean {
        init(packageName)
        val type = getComponentType(packageName, componentName)
        val result = when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> controller.add(packageName, componentName, type)
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> controller.remove(packageName, componentName, type)
            else -> false
        }
        if (result) {
            controller.save()
        }
        return result;
    }

    override fun enable(packageName: String, componentName: String): Boolean {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    }

    override fun disable(packageName: String, componentName: String): Boolean {
        return switchComponent(packageName, componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    override fun batchEnable(componentList: List<ComponentName>): Int {
        var succeededCount = 0
        componentList.forEach {
            init(it.packageName)
            val type = getComponentType(it.packageName, it.className)
            if (controller.add(it.packageName, it.className, type)) {
                succeededCount++
            }
        }
        controller.save()
        return succeededCount
    }

    override fun batchDisable(componentList: List<ComponentName>): Int {
        var succeededCount = 0
        componentList.forEach {
            init(it.packageName)
            val type = getComponentType(it.packageName, it.className)
            if (controller.remove(it.packageName, it.className, type)) {
                succeededCount++
            }
        }
        controller.save()
        return succeededCount
    }

    override fun checkComponentEnableState(packageName: String, componentName: String): Boolean {
        return controller.getComponentEnableState(packageName, componentName)
    }

    private fun init(packageName: String) {
        initController(packageName)
        initPackageInfo(packageName)
    }

    private fun initController(packageName: String) {
        if (!::controller.isInitialized || controller.packageName != packageName) {
            controller = IntentFirewallImpl.getInstance(context, packageName)
            return
        }
    }

    private fun initPackageInfo(packageName: String) {
        if (!::packageInfo.isInitialized) {
            packageInfo = ApplicationComponents.getApplicationComponents(context.packageManager, packageName)
        }
    }

    private fun getComponentType(packageName: String, componentName: String): ComponentType {
        packageInfo.receivers?.forEach {
            if (it.name == componentName) {
                return ComponentType.BROADCAST
            }
        }
        packageInfo.services?.forEach {
            if (it.name == componentName) {
                return ComponentType.SERVICE
            }
        }
        packageInfo.activities?.forEach {
            if (it.name == componentName) {
                return ComponentType.ACTIVITY
            }
        }
        packageInfo.providers?.forEach {
            if (it.name == componentName) {
                return ComponentType.PROVIDER
            }
        }
        return ComponentType.UNKNOWN
    }
}