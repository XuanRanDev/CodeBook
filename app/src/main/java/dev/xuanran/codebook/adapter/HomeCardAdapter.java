package dev.xuanran.codebook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.ButterKnife;
import dev.xuanran.codebook.R;
import dev.xuanran.codebook.provider.HomeCardProvider;

/**
 * Created By XuanRan on 2022/3/19
 */
public class HomeCardAdapter extends BaseNodeAdapter {


    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> list, int i) {
        return 0;
    }

    public HomeCardAdapter() {
        addFullSpanNodeProvider(new HomeCardProvider());
    }
}
