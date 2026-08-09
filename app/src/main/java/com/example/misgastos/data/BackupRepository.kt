package com.example.misgastos.data

import androidx.room.withTransaction
import com.example.misgastos.data.local.AppDatabase
import com.example.misgastos.data.local.AppSettings
import com.example.misgastos.data.local.Category
import com.example.misgastos.data.local.ThemeMode
import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionType
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import org.json.JSONArray
import org.json.JSONObject

private const val BACKUP_FORMAT = "mis_gastos_backup"
private const val BACKUP_VERSION = 1

class BackupRepository(
    private val database: AppDatabase
) {
    suspend fun export(output: OutputStream) {
        val json = database.withTransaction {
            buildBackupJson(
                settings = database.settingsDao().getSettings() ?: AppSettings(),
                categories = database.settingsDao().getAllCategories(),
                transactions = database.transactionDao().getAll()
            )
        }

        output.write(json.toString(2).toByteArray(Charsets.UTF_8))
    }

    suspend fun import(input: InputStream) {
        val json = input.readBytes().toString(Charsets.UTF_8)
        val backup = parseBackup(json)

        database.withTransaction {
            database.transactionDao().deleteAll()
            database.settingsDao().deleteAllCategories()
            database.settingsDao().upsertSettings(backup.settings)
            database.settingsDao().insertCategories(backup.categories)
            database.transactionDao().insertAll(backup.transactions)
        }
    }

    private fun buildBackupJson(
        settings: AppSettings,
        categories: List<Category>,
        transactions: List<Transaction>
    ): JSONObject = JSONObject().apply {
        put("format", BACKUP_FORMAT)
        put("version", BACKUP_VERSION)
        put("settings", JSONObject().apply {
            put("id", settings.id)
            put("decimalPlaces", settings.decimalPlaces)
            put("savingsGoalCents", settings.savingsGoalCents)
            put("themeMode", settings.themeMode.name)
        })
        put("categories", JSONArray().apply {
            categories.forEach { category ->
                put(JSONObject().apply {
                    put("id", category.id)
                    put("name", category.name)
                    put("type", category.type.name)
                    put("iconName", category.iconName)
                })
            }
        })
        put("transactions", JSONArray().apply {
            transactions.forEach { transaction ->
                put(JSONObject().apply {
                    put("id", transaction.id)
                    put("title", transaction.title)
                    put("amountCents", transaction.amountCents)
                    put("category", transaction.category)
                    put("date", transaction.date)
                    put("type", transaction.type.name)
                })
            }
        })
    }

    private fun parseBackup(json: String): BackupData {
        val root = runCatching { JSONObject(json) }
            .getOrElse { throw InvalidBackupException() }
        if (root.optString("format") != BACKUP_FORMAT ||
            root.optInt("version", -1) != BACKUP_VERSION
        ) {
            throw InvalidBackupException()
        }

        val settingsJson = root.optJSONObject("settings") ?: throw InvalidBackupException()
        val settings = AppSettings(
            id = settingsJson.requiredInt("id").also { if (it != 1) throw InvalidBackupException() },
            decimalPlaces = settingsJson.requiredInt("decimalPlaces")
                .also { if (it !in 0..2) throw InvalidBackupException() },
            savingsGoalCents = settingsJson.requiredLong("savingsGoalCents")
                .also { if (it < 0) throw InvalidBackupException() },
            themeMode = settingsJson.requiredEnum<ThemeMode>("themeMode")
        )

        val categoriesJson = root.optJSONArray("categories") ?: throw InvalidBackupException()
        val categoryIds = mutableSetOf<Long>()
        val categoryKeys = mutableSetOf<String>()
        val categories = buildList {
            for (index in 0 until categoriesJson.length()) {
                val categoryJson = categoriesJson.optJSONObject(index)
                    ?: throw InvalidBackupException()
                val id = categoryJson.requiredLong("id")
                val name = categoryJson.requiredString("name")
                val type = categoryJson.requiredEnum<TransactionType>("type")
                val iconName = categoryJson.requiredString("iconName")
                val key = "${type.name}:${name.lowercase()}"
                if (!categoryIds.add(id) || !categoryKeys.add(key)) {
                    throw InvalidBackupException()
                }
                add(Category(id = id, name = name, type = type, iconName = iconName))
            }
        }

        val transactionsJson = root.optJSONArray("transactions") ?: throw InvalidBackupException()
        val transactionIds = mutableSetOf<Long>()
        val transactions = buildList {
            for (index in 0 until transactionsJson.length()) {
                val transactionJson = transactionsJson.optJSONObject(index)
                    ?: throw InvalidBackupException()
                val id = transactionJson.requiredLong("id")
                val title = transactionJson.requiredString("title")
                val amountCents = transactionJson.requiredLong("amountCents")
                val category = transactionJson.requiredString("category")
                val date = transactionJson.requiredLong("date")
                val type = transactionJson.requiredEnum<TransactionType>("type")
                if (id <= 0 || amountCents <= 0 || !transactionIds.add(id)) {
                    throw InvalidBackupException()
                }
                add(
                    Transaction(
                        id = id,
                        title = title,
                        amountCents = amountCents,
                        category = category,
                        date = date,
                        type = type
                    )
                )
            }
        }

        return BackupData(settings, categories, transactions)
    }

    private data class BackupData(
        val settings: AppSettings,
        val categories: List<Category>,
        val transactions: List<Transaction>
    )

    private class InvalidBackupException : IOException("Archivo de respaldo inválido")
}

private fun JSONObject.requiredString(key: String): String =
    if (!has(key) || isNull(key)) {
        throw IOException("Falta el campo $key")
    } else {
        getString(key).trim().takeIf { it.isNotEmpty() }
            ?: throw IOException("El campo $key está vacío")
    }

private fun JSONObject.requiredInt(key: String): Int =
    if (!has(key) || isNull(key)) throw IOException("Falta el campo $key") else getInt(key)

private fun JSONObject.requiredLong(key: String): Long =
    if (!has(key) || isNull(key)) throw IOException("Falta el campo $key") else getLong(key)

private inline fun <reified T : Enum<T>> JSONObject.requiredEnum(key: String): T =
    runCatching { enumValueOf<T>(requiredString(key)) }
        .getOrElse { throw IOException("Valor inválido en $key") }
