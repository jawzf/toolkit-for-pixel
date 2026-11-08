package com.jawzf.pixeltoolkit.core.platform.device

@Suppress("UNCHECKED_CAST")
fun <T> parseZenRules(result: Any?): Map<String, T> =
    when (result) {
        null -> emptyMap()
        is Map<*, *> -> result as Map<String, T>
        else -> {
            val list = result.javaClass.getMethod("getList").invoke(result) as List<Any>
            list.associate { item ->
                val cls = item.javaClass
                val id = cls.getDeclaredField("mId").apply { isAccessible = true }.get(item) as String
                val rule = cls.getDeclaredField("mRule").apply { isAccessible = true }.get(item) as T
                id to rule
            }
        }
    }
