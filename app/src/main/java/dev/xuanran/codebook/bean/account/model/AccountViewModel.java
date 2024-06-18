package dev.xuanran.codebook.bean.account.model;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import javax.crypto.SecretKey;

import dev.xuanran.codebook.activity.MainActivity;
import dev.xuanran.codebook.bean.account.AccountEntity;
import dev.xuanran.codebook.bean.account.AccountRepository;
import dev.xuanran.codebook.callback.ExportCallback;
import dev.xuanran.codebook.callback.ImportCallback;
import dev.xuanran.codebook.util.AESUtils;
import dev.xuanran.codebook.util.JsonUtil;
import dev.xuanran.codebook.util.PasswordUtils;

public class AccountViewModel extends AndroidViewModel {
    private AccountRepository repository;
    private LiveData<List<AccountEntity>> allAccounts;

    public AccountViewModel(@NonNull Application application) {
        super(application);
        repository = new AccountRepository(application);
        allAccounts = Transformations.map(repository.getAllAccounts(), this::decryptAccounts);
    }

    private List<AccountEntity> decryptAccounts(List<AccountEntity> accounts) {
        if (accounts != null) {
            for (AccountEntity account : accounts) {
                String username = account.getUsername();
                String decryptData = MainActivity.cipherStrategy.decryptData(username);
                account.setUsername(decryptData);
                account.setPassword(MainActivity.cipherStrategy.decryptData(account.getPassword()));
            }
        }
        return accounts;
    }

    public void insert(AccountEntity account) {
        account.setUsername(MainActivity.cipherStrategy.encryptData(account.getUsername()));
        account.setPassword(MainActivity.cipherStrategy.encryptData(account.getPassword()));
        repository.insert(account);
    }

    public void update(AccountEntity account) {
        account.setUsername(MainActivity.cipherStrategy.encryptData(account.getUsername()));
        account.setPassword(MainActivity.cipherStrategy.encryptData(account.getPassword()));
        repository.update(account);
    }

    public void delete(AccountEntity account) {
        repository.delete(account);
    }

    public LiveData<List<AccountEntity>> getAllAccounts() {
        return allAccounts;
    }

    public LiveData<List<AccountEntity>> searchAccounts(String query) {
        return Transformations.map(repository.searchAccounts(query), this::decryptAccounts);
    }

    public void exportData(SecretKey secretKey, Uri uri, ExportCallback callback) {
        new Thread(() -> {
            try {
                List<AccountEntity> accounts = allAccounts.getValue();
                String data = JsonUtil.convertListToJson(accounts);
                String encryptedData = AESUtils.encrypt(secretKey, data);

                try (OutputStream outputStream = getApplication().getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        outputStream.write(encryptedData.getBytes());
                    }
                }

                callback.onSuccess(new File(uri.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e);
            }
        }).start();
    }


    public void importData(SecretKey secretKey, Uri uri, ImportCallback callback) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getApplication().getContentResolver().openInputStream(uri)))) {
                StringBuilder data = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }

                String decryptedData = AESUtils.decrypt(secretKey, data.toString());

                List<AccountEntity> accountEntities = JsonUtil.convertJsonToList(decryptedData);

                for (AccountEntity accountEntity : accountEntities) {
                    accountEntity.setUsername(MainActivity.cipherStrategy.encryptData(accountEntity.getUsername()));
                    accountEntity.setPassword(MainActivity.cipherStrategy.encryptData(accountEntity.getPassword()));
                    repository.insert(accountEntity);
                }
                callback.onSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e);
            }
        }).start();
    }
}
