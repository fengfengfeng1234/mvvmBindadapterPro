package me.tatarka.bindingcollectionadapter.sample;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import me.tatarka.bindingcollectionadapter.sample.databinding.ItemttBinding;
import me.tatarka.bindingcollectionadapter2.ItemVM;


public class ItemViewModel  extends ItemVM<Bean, ItemttBinding> {
    public boolean checkable;
    private int index;
    private final MutableLiveData<Boolean> checked;

    public ItemViewModel() {
        checked = new MutableLiveData<>();
        checked.setValue(false);
    }

    public int getIndex() {
        return index;
    }

    public LiveData<Boolean> isChecked() {
        return checked;
    }

    public void setChecked(boolean value) {
        if (!checkable) {
            return;
        }
        checked.setValue(value);
    }

    @MainThread
    public boolean onToggleChecked() {
        if (!checkable) {
            return false;
        }
        checked.setValue(!checked.getValue());
        return true;
    }



    @Override
    protected void onBindViewHolder(@NonNull ItemttBinding viewDataBinding, @NonNull Bean item) {
        index=item.index;


    }

    @Override
    protected int getVariableId() {
        return BR.item;
    }

    @Override
    protected int getLayout() {
        return R.layout.itemtt;
    }
}
