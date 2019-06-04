package me.tatarka.bindingcollectionadapter.sample;

import android.support.annotation.NonNull;

import me.tatarka.bindingcollectionadapter.sample.databinding.ItemChatLeftBinding;
import me.tatarka.bindingcollectionadapter.sample.databinding.ItemChatRightBinding;
import me.tatarka.bindingcollectionadapter2.ItemVM;

/**
 * Time:2019-06-03
 * Create : Lipeng.tao
 * Describe :
 */
public class RightChatVM extends ItemVM<ChatBean, ItemChatRightBinding> {

    public String content;

    @Override
    protected void onBindViewHolder(@NonNull ItemChatRightBinding viewDataBinding, @NonNull ChatBean item) {
        content = item.conetent;
    }

    @Override
    protected int getVariableId() {
        return BR.item;
    }

    @Override
    protected int getLayout() {
        return R.layout.item_chat_right;
    }
}
