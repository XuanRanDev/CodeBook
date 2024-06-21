package dev.xuanran.codebook.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dev.xuanran.codebook.bean.account.AccountEntity;

public class JsonUtil {

    public static String convertListToJson(List<AccountEntity> accounts) {
        JSONArray jsonArray = new JSONArray();
        for (AccountEntity account : accounts) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("appName", account.getAppName());
                jsonObject.put("username", account.getUsername());
                jsonObject.put("password", account.getPassword());
                jsonObject.put("remark", account.getRemark());
                jsonObject.put("createTime", account.getCreateTime().getTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray.toString();
    }

    public static List<AccountEntity> convertJsonToList(String jsonString) {
        List<AccountEntity> accounts = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String appName = jsonObject.optString("appName", "");
                String username = jsonObject.optString("username", "");
                String password = jsonObject.optString("password", "");
                String remark = jsonObject.optString("remark", "");
                long createTimeLong = jsonObject.getLong("createTime");
                AccountEntity account = new AccountEntity(appName, username, password, remark, new Date(createTimeLong));
                accounts.add(account);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return accounts;
    }
}
