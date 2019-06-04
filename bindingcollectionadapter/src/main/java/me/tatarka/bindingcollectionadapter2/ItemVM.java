package me.tatarka.bindingcollectionadapter2;

import android.arch.lifecycle.ViewModel;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;

/**
 * Time:2019-06-03
 * Create : Lipeng.tao
 * Describe :
 */
public abstract class ItemVM<T,VM extends ViewDataBinding> extends ViewModel {



    protected abstract void onBindViewHolder(@NonNull VM viewDataBinding, @NonNull T item);


    protected abstract  int getVariableId();


    protected abstract  int getLayout();

    protected   int position;


}