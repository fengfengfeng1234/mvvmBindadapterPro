package me.tatarka.bindingcollectionadapter2;

import android.arch.lifecycle.LifecycleOwner;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import me.tatarka.bindingcollectionadapter2.multitype.BinderNotFoundException;
import me.tatarka.bindingcollectionadapter2.multitype.DefaultLinker;
import me.tatarka.bindingcollectionadapter2.multitype.Linker;
import me.tatarka.bindingcollectionadapter2.multitype.TypePool;

/**
 * A {@link RecyclerView.Adapter} that binds items to layouts using the given {@link ItemBinding}.
 * If you give it an {@link ObservableList} it will also updated itself based on changes to that
 * list.
 */
public class BindingRecyclerViewAdapter<T> extends RecyclerView.Adapter<ViewHolder> implements BindingCollectionAdapter<T> {
    private static final Object DATA_INVALIDATION = new Object();
    public static final String TAG = "BindingRecyclerViewAdapter";
    //    private ItemBinding<T> itemBinding;
    private WeakReferenceOnListChangedCallback<T> callback;
    private List<T> items;
    private LayoutInflater inflater;
    private ItemIds<? super T> itemIds;
    private ViewHolderFactory viewHolderFactory;
    // Currently attached recyclerview, we don't have to listen to notifications if null.
    @Nullable
    private RecyclerView recyclerView;
    @Nullable
    private LifecycleOwner lifecycleOwner;

    private @NonNull
    TypePool typePool;


    public void setTypePool(@NonNull TypePool typePool) {
        this.typePool = typePool;
    }


    /**
     * Sets the lifecycle owner of this adapter to work with {@code LiveData}.
     * This is normally not necessary, but due to an androidx limitation, you need to set this if
     * the containing view is <em>not</em> using databinding.
     */
    public void setLifecycleOwner(@Nullable LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
        if (recyclerView != null) {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View child = recyclerView.getChildAt(i);
                ViewDataBinding binding = DataBindingUtil.getBinding(child);
                if (binding != null) {
                    binding.setLifecycleOwner(lifecycleOwner);
                }
            }
        }
    }


    @Override
    public void setItemBinding(ItemBinding<T> itemBinding) {

    }

    @Override
    public ItemBinding<T> getItemBinding() {
        return null;
    }

    @Override
    public void setItems(@Nullable List<T> items) {
        if (this.items == items) {
            return;
        }
        // If a recyclerview is listening, set up listeners. Otherwise wait until one is attached.
        // No need to make a sound if nobody is listening right?
        if (recyclerView != null) {
            if (this.items instanceof ObservableList) {
                ((ObservableList<T>) this.items).removeOnListChangedCallback(callback);
                callback = null;
            }
            if (items instanceof ObservableList) {
                callback = new WeakReferenceOnListChangedCallback<>(this, (ObservableList<T>) items);
                ((ObservableList<T>) items).addOnListChangedCallback(callback);
            }
        }
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public T getAdapterItem(int position) {
        return items.get(position);
    }

    @Override
    public ViewDataBinding onCreateBinding(LayoutInflater inflater, @LayoutRes int layoutId, ViewGroup viewGroup) {
        return DataBindingUtil.inflate(inflater, layoutId, viewGroup, false);
    }


    public void onBindBinding(ViewDataBinding binding, int position, T item, ItemVM itemVM) {
        boolean result = binding.setVariable(itemVM.getVariableId(), itemVM);
        itemVM.onBindViewHolder(binding, item);
        itemVM.position = position;
        if (result) {
            binding.executePendingBindings();
        }

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        if (this.recyclerView == null && items instanceof ObservableList) {
            callback = new WeakReferenceOnListChangedCallback<>(this, (ObservableList<T>) items);
            ((ObservableList<T>) items).addOnListChangedCallback(callback);
        }
        this.recyclerView = recyclerView;
        if (lifecycleOwner == null) {
            lifecycleOwner = Utils.findLifecycleOwner(recyclerView);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if (this.recyclerView != null && items instanceof ObservableList) {
            ((ObservableList<T>) items).removeOnListChangedCallback(callback);
            callback = null;
        }
        this.recyclerView = null;
    }

    @NonNull
    @Override
    public final ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int indexViewType) {
        if (inflater == null) {
            inflater = LayoutInflater.from(viewGroup.getContext());
        }

        ItemVM<?, ?> binder = typePool.getItemViewBinder(indexViewType);

        ViewDataBinding binding = onCreateBinding(inflater, binder.getLayout(), viewGroup);
        final ViewHolder holder = onCreateViewHolder(binding);
        binding.addOnRebindCallback(new OnRebindCallback() {
            @Override
            public boolean onPreBind(ViewDataBinding binding) {
                return recyclerView != null && recyclerView.isComputingLayout();
            }

            @Override
            public void onCanceled(ViewDataBinding binding) {
                if (recyclerView == null || recyclerView.isComputingLayout()) {
                    return;
                }
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position, DATA_INVALIDATION);
                }
            }
        });
        return holder;
    }

    /**
     * Constructs a view holder for the given databinding. The default implementation is to use
     * {@link ViewHolderFactory} if provided, otherwise use a default view holder.
     */
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewDataBinding binding) {
        if (viewHolderFactory != null) {
            return viewHolderFactory.createViewHolder(binding);
        } else {
            return new BindingViewHolder(binding);
        }
    }

    private static class BindingViewHolder extends ViewHolder {
        BindingViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
        }
    }

    @Override
    public final void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        // This won't be called by recyclerview since we are overriding the other overload, call
        // the other overload here in case someone is calling this directly ex: in a test.
        onBindViewHolder(viewHolder, position, Collections.emptyList());
    }

    @Override
    @CallSuper
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        ViewDataBinding binding = DataBindingUtil.getBinding(holder.itemView);
        if (isForDataBinding(payloads)) {
            binding.executePendingBindings();
        } else {
            binding.setLifecycleOwner(lifecycleOwner);
            T item = items.get(position);
            ItemVM binder = typePool.getItemViewBinder(holder.getItemViewType());
            onBindBinding(binding, position, item, binder);
        }
    }

    private boolean isForDataBinding(List<Object> payloads) {
        if (payloads == null || payloads.size() == 0) {
            return false;
        }
        for (int i = 0; i < payloads.size(); i++) {
            Object obj = payloads.get(i);
            if (obj != DATA_INVALIDATION) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        return indexInTypesOf(position, item);
    }

    int indexInTypesOf(int position, @NonNull Object item) throws BinderNotFoundException {
        /**
         * 找到 实体对象 所在集合下标。
         */
        int index = typePool.firstIndexOf(item.getClass());
        if (index != -1) {
            @SuppressWarnings("unchecked")
            /**
             *    不管是一对一 还是 一对多。
             *    都装入集合中。
             */
                    Linker<Object> linker = (Linker<Object>) typePool.getLinker(index);
            return index + linker.index(position, item);
        }
        throw new BinderNotFoundException(item.getClass());
    }

    /**
     * Set the itemtt id's for the items. If not null, this will set {@link
     * android.support.v7.widget.RecyclerView.Adapter#setHasStableIds(boolean)} to true.
     */
    public void setItemIds(@Nullable ItemIds<? super T> itemIds) {
        if (this.itemIds != itemIds) {
            this.itemIds = itemIds;
            setHasStableIds(itemIds != null);
        }
    }

    /**
     * Set the factory for creating view holders. If null, a default view holder will be used. This
     * is useful for holding custom state in the view holder or other more complex customization.
     */
    public void setViewHolderFactory(@Nullable ViewHolderFactory factory) {
        viewHolderFactory = factory;
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public long getItemId(int position) {
        return itemIds == null ? position : itemIds.getItemId(position, items.get(position));
    }

    private static class WeakReferenceOnListChangedCallback<T> extends ObservableList.OnListChangedCallback<ObservableList<T>> {
        final WeakReference<BindingRecyclerViewAdapter<T>> adapterRef;

        WeakReferenceOnListChangedCallback(BindingRecyclerViewAdapter<T> adapter, ObservableList<T> items) {
            this.adapterRef = AdapterReferenceCollector.createRef(adapter, items, this);
        }

        @Override
        public void onChanged(ObservableList sender) {
            BindingRecyclerViewAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            Utils.ensureChangeOnMainThread();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(ObservableList sender, final int positionStart, final int itemCount) {
            BindingRecyclerViewAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            Utils.ensureChangeOnMainThread();
            adapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(ObservableList sender, final int positionStart, final int itemCount) {
            BindingRecyclerViewAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            Utils.ensureChangeOnMainThread();
            adapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(ObservableList sender, final int fromPosition, final int toPosition, final int itemCount) {
            BindingRecyclerViewAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            Utils.ensureChangeOnMainThread();
            for (int i = 0; i < itemCount; i++) {
                adapter.notifyItemMoved(fromPosition + i, toPosition + i);
            }
        }

        @Override
        public void onItemRangeRemoved(ObservableList sender, final int positionStart, final int itemCount) {
            BindingRecyclerViewAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            Utils.ensureChangeOnMainThread();
            adapter.notifyItemRangeRemoved(positionStart, itemCount);
        }
    }

    public interface ItemIds<T> {
        long getItemId(int position, T item);
    }

    public interface ViewHolderFactory {
        RecyclerView.ViewHolder createViewHolder(ViewDataBinding binding);
    }


    public <T> void register(
            @NonNull Class<? extends T> clazz,
            @NonNull ItemVM<T, ?> binder,
            @NonNull Linker<T> linker) {
        typePool.register(clazz, binder, linker);
    }

    /**
     * Registers a type class and its itemtt view binder. If you have registered the class,
     * it will override the original binder(s). Note that the method is non-thread-safe
     * so that you should not use it in concurrent operation.
     * <p>
     * Note that the method should not be called after
     * {@link RecyclerView#setAdapter(RecyclerView.Adapter)}, or you have to call the setAdapter
     * again.
     * </p>
     *
     * @param clazz  the class of a itemtt
     * @param binder the itemtt view binder
     * @param <T>    the itemtt data type
     */
    public <T> void register(@NonNull Class<? extends T> clazz, @NonNull ItemVM<T, ?> binder) {
        checkAndRemoveAllTypesIfNeeded(clazz);
        register(clazz, binder, new DefaultLinker<T>());
    }

    private void checkAndRemoveAllTypesIfNeeded(@NonNull Class<?> clazz) {
        if (typePool.unregister(clazz)) {
            Log.w(TAG, "You have registered the " + clazz.getSimpleName() + " type. " +
                    "It will override the original binder(s).");
        }
    }


}
