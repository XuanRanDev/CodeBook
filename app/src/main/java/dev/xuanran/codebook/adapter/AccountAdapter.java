package dev.xuanran.codebook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import dev.xuanran.codebook.R;
import dev.xuanran.codebook.bean.AccountEntity;

public class AccountAdapter extends RecyclerView.Adapter<AccountViewHolder> {
    private List<AccountEntity> accounts = new ArrayList<>();

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_item, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        AccountEntity currentAccount = accounts.get(position);
        holder.title.setText(currentAccount.getName());
        holder.id.setText(String.valueOf(currentAccount.getId()));
        holder.createDate.setText("Create Date"); // replace with actual date
        holder.tagText.setText(currentAccount.getTags());

        holder.viewButton.setOnClickListener(v -> {
            // Handle view button click
            Toast.makeText(v.getContext(), "View button clicked for " + currentAccount.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    public AccountEntity getAccountAt(int position) {
        return accounts.get(position);
    }

    public void filter(String text) {
        List<AccountEntity> filteredList = new ArrayList<>();
        for (AccountEntity account : accounts) {
            // 根据条件过滤数据
            if (account.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(account);
            }
        }
        // 更新数据集
        accounts = filteredList;
        notifyDataSetChanged();
    }

}
