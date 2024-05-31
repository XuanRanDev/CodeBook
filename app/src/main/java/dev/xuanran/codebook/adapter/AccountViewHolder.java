package dev.xuanran.codebook.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import dev.xuanran.codebook.R;

public class AccountViewHolder extends RecyclerView.ViewHolder {
    MaterialCardView cardView;
    TextView title;
    TextView id;
    ImageView imgDate;
    TextView createDate;
    ImageView imgTag;
    TextView tagText;
    Button viewButton;

    public AccountViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.list_cardView_card);
        title = itemView.findViewById(R.id.list_cardView_title);
        id = itemView.findViewById(R.id.list_cardView_id);
        imgDate = itemView.findViewById(R.id.list_cardView_img_date);
        createDate = itemView.findViewById(R.id.list_cardView_createDate);
        imgTag = itemView.findViewById(R.id.list_cardView_img_tag);
        tagText = itemView.findViewById(R.id.list_cardView_tag_text);
        viewButton = itemView.findViewById(R.id.list_cardView_view);
    }
}
