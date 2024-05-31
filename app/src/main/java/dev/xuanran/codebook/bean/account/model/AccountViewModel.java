package dev.xuanran.codebook.bean.account.model;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.List;

import dev.xuanran.codebook.bean.account.AccountEntity;
import dev.xuanran.codebook.bean.account.AccountRepository;
import dev.xuanran.codebook.callback.ExportCallback;
import dev.xuanran.codebook.callback.ImportCallback;
import dev.xuanran.codebook.util.CryptoUtils;

public class AccountViewModel extends AndroidViewModel {
    private AccountRepository repository;
    private LiveData<List<AccountEntity>> allAccounts;

    public AccountViewModel(@NonNull Application application) {
        super(application);
        repository = new AccountRepository(application);
        allAccounts = repository.getAllAccounts();
    }

    public void insert(AccountEntity account) {
        repository.insert(account);
    }

    public void update(AccountEntity account) {
        repository.update(account);
    }

    public void delete(AccountEntity account) {
        repository.delete(account);
    }

    public LiveData<List<AccountEntity>> getAllAccounts() {
        return allAccounts;
    }

    public LiveData<List<AccountEntity>> searchAccounts(String query) {
        return repository.searchAccounts(query);
    }


    public void exportData(String password, ExportCallback callback) {
        new Thread(() -> {
            try {
                List<AccountEntity> accounts = allAccounts.getValue();
                StringBuilder data = new StringBuilder();
                for (AccountEntity account : accounts) {
                    data.append(account.toString()).append("\n");
                }
                String encryptedData = CryptoUtils.encrypt(data.toString(), password);

                File file = new File(getApplication().getExternalFilesDir(null), "accounts_backup.txt");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(encryptedData.getBytes());
                }

                callback.onSuccess(file);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e);
            }
        }).start();
    }

    public void importData(String password, Uri uri, ImportCallback callback) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getApplication().getContentResolver().openInputStream(uri)))) {
                StringBuilder data = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }
                String decryptedData = CryptoUtils.decrypt(data.toString(), password);
                String[] accounts = decryptedData.split("\n");

//                repository.deleteAll();
                for (String accountData : accounts) {
                    AccountEntity account = AccountEntity.fromString(accountData);
                    repository.insert(account);
                }

                callback.onSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e);
            }
        }).start();
    }

}
