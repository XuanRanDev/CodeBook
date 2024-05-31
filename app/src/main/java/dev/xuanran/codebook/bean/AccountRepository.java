package dev.xuanran.codebook.bean;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import dev.xuanran.codebook.db.AccountDatabase;

public class AccountRepository {
    private AccountDao accountDao;
    private LiveData<List<AccountEntity>> allAccounts;

    public AccountRepository(Application application) {
        AccountDatabase database = AccountDatabase.getInstance(application);
        accountDao = database.accountDao();
        allAccounts = accountDao.getAllAccounts();
    }

    public void insert(AccountEntity account) {
        new InsertAccountAsyncTask(accountDao).execute(account);
    }

    public void update(AccountEntity account) {
        new UpdateAccountAsyncTask(accountDao).execute(account);
    }

    public void delete(AccountEntity account) {
        new DeleteAccountAsyncTask(accountDao).execute(account);
    }

    public LiveData<List<AccountEntity>> getAllAccounts() {
        return allAccounts;
    }

    private static class InsertAccountAsyncTask extends AsyncTask<AccountEntity, Void, Void> {
        private AccountDao accountDao;

        private InsertAccountAsyncTask(AccountDao accountDao) {
            this.accountDao = accountDao;
        }

        @Override
        protected Void doInBackground(AccountEntity... accounts) {
            accountDao.insert(accounts[0]);
            return null;
        }
    }

    private static class UpdateAccountAsyncTask extends AsyncTask<AccountEntity, Void, Void> {
        private AccountDao accountDao;

        private UpdateAccountAsyncTask(AccountDao accountDao) {
            this.accountDao = accountDao;
        }

        @Override
        protected Void doInBackground(AccountEntity... accounts) {
            accountDao.update(accounts[0]);
            return null;
        }
    }

    private static class DeleteAccountAsyncTask extends AsyncTask<AccountEntity, Void, Void> {
        private AccountDao accountDao;

        private DeleteAccountAsyncTask(AccountDao accountDao) {
            this.accountDao = accountDao;
        }

        @Override
        protected Void doInBackground(AccountEntity... accounts) {
            accountDao.delete(accounts[0]);
            return null;
        }
    }
}
