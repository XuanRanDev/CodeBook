package dev.xuanran.codebook.model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import dev.xuanran.codebook.bean.AccountEntity;
import dev.xuanran.codebook.bean.AccountRepository;

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
}
