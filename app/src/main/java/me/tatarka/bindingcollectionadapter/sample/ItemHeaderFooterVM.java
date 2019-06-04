package me.tatarka.bindingcollectionadapter.sample;

import android.support.annotation.NonNull;

import me.tatarka.bindingcollectionadapter.sample.databinding.ItemHeaderFooterBinding;
import me.tatarka.bindingcollectionadapter2.ItemVM;

/**
 * Time:2019-06-03
 * Create : Lipeng.tao
 * Describe :
 */
public class ItemHeaderFooterVM extends ItemVM<String, ItemHeaderFooterBinding> {

    public String title;

    @Override
    protected void onBindViewHolder(@NonNull ItemHeaderFooterBinding viewDataBinding, @NonNull String item) {
        title = item;
    }

    @Override
    protected int getVariableId() {
        return BR.item;
    }

    @Override
    protected int getLayout() {
        return R.layout.item_header_footer;
    }
}
