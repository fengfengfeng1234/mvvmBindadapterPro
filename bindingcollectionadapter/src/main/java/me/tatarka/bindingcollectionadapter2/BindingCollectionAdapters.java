package me.tatarka.bindingcollectionadapter2;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewPager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.WrapperListAdapter;

import java.util.List;

/**
 * All the BindingAdapters so that you can set your adapters and items directly in your layout.
 */
public class BindingCollectionAdapters {


    /**
     * Unwraps any {@link android.widget.WrapperListAdapter}, commonly {@link
     * android.widget.HeaderViewListAdapter}.
     */
    private static Adapter unwrapAdapter(Adapter adapter) {
        return adapter instanceof WrapperListAdapter
                ? unwrapAdapter(((WrapperListAdapter) adapter).getWrappedAdapter())
                : adapter;
    }



    @BindingConversion
    public static <T> ItemBinding<T> toItemBinding(OnItemBind<T> onItemBind) {
        return ItemBinding.of(onItemBind);
    }
}
