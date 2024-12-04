package dev.xuanran.codebook.util

import dev.xuanran.codebook.bean.account.AccountEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Date

object JsonUtil {
    fun convertListToJson(accounts: MutableList<AccountEntity>): String {
        val jsonArray = JSONArray()
        for (account in accounts) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("appName", account.getAppName())
                jsonObject.put("username", account.getUsername())
                jsonObject.put("password", account.getPassword())
                jsonObject.put("remark", account.getRemark())
                jsonObject.put("createTime", account.getCreateTime().getTime())
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @JvmStatic
    fun convertJsonToList(jsonString: String?): MutableList<AccountEntity?> {
        val accounts: MutableList<AccountEntity?> = ArrayList<AccountEntity?>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val appName = jsonObject.optString("appName", "")
                val username = jsonObject.optString("username", "")
                val password = jsonObject.optString("password", "")
                val remark = jsonObject.optString("remark", "")
                val createTimeLong = jsonObject.getLong("createTime")
                val account =
                    AccountEntity(appName, username, password, remark, Date(createTimeLong))
                accounts.add(account)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return accounts
    }
}
