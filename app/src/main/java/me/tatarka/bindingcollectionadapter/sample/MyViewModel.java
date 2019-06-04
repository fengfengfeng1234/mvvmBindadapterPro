package me.tatarka.bindingcollectionadapter.sample;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;

import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.ItemVM;
import me.tatarka.bindingcollectionadapter2.OnItemBind;
import me.tatarka.bindingcollectionadapter2.collections.MergeObservableList;
import me.tatarka.bindingcollectionadapter2.multitype.ClassLinker;
import me.tatarka.bindingcollectionadapter2.multitype.MultiTypePool;

public class MyViewModel extends ViewModel {

    public final ObservableList<Object> items = new ObservableArrayList<>();
    private boolean checkable;

    /**
     * Items merged with a header on top and footer on bottom.
     */
    public final MergeObservableList<Object> headerFooterItems = new MergeObservableList<>()
            .insertItem("Header")
            .insertList(items)
            .insertItem("Footer");

    public final MultiTypePool typePool = new MultiTypePool();

    {
        typePool.register(Bean.class, new ItemViewModel());
        typePool.register(String.class, new ItemHeaderFooterVM());
        typePool.register(ChatBean.class)
                .to(new LeftChatVM(), new RightChatVM())
                .withClassLinker(new ClassLinker<ChatBean>() {
                    @NonNull
                    @Override
                    public Class<? extends ItemVM<ChatBean, ?>> index(int position, @NonNull ChatBean chatBean) {
                        if (chatBean.type == 1) {
                            return LeftChatVM.class;
                        } else {
                            return RightChatVM.class;
                        }
                    }
                });
    }


    public MyViewModel() {
        for (int i = 0; i < 3; i++) {
            Bean bean = new Bean();
            bean.index = items.size();
            items.add(bean);
        }
        ChatBean left = new ChatBean();
        left.type = 1;
        left.conetent = "left";
        items.add(left);

        ChatBean right = new ChatBean();
        right.type = 2;
        right.conetent = "right";
        items.add(right);

    }

    public void setCheckable(boolean checkable) {
//        this.checkable = checkable;
//        for (Bean item : items) {
//            item.checkable = checkable;
//        }
    }

    /**
     * Binds a homogeneous list of items to a layout.
     */
//    public final ItemBinding<ItemViewModel> singleItem = ItemBinding.of(BR.itemtt, R.layout.itemtt);
//
//    public final ItemBinding<ItemViewModel> pageItem = ItemBinding.of(BR.itemtt, R.layout.item_page);


//    public final OnItemBind<Object> multipleItems = new OnItemBind<Object>() {
//        @Override
//        public void onItemBind(ItemBinding itemBinding, int position, Object item) {
//            if (String.class.equals(item.getClass())) {
//                itemBinding.set(BR.item, R.layout.item_header_footer);
//            } else if (ItemViewModel.class.equals(item.getClass())) {
//                itemBinding.set(BR.item, R.layout.itemtt);
//            }
//        }
//    };

//    public final OnItemBindClass<Object> multipleItems = new OnItemBindClass<>()
//            .map(String.class, BR.itemtt, R.layout.item_header_footer)
//            .map(ItemViewModel.class, BR.itemtt, R.layout.itemtt);

    /**
     * Define stable itemtt ids. These are just based on position because the items happen to not
     * every move around.
     */
//    public final BindingListViewAdapter.ItemIds<Object> itemIds = new BindingListViewAdapter.ItemIds<Object>() {
//        @Override
//        public long getItemId(int position, Object itemtt) {
//            return position;
//        }
//    };

    /**
     * Define page titles for a ViewPager
     */
//    public final BindingViewPagerAdapter.PageTitles<ItemViewModel> pageTitles = new BindingViewPagerAdapter.PageTitles<ItemViewModel>() {
//        @Override
//        public CharSequence getPageTitle(int position, ItemViewModel itemtt) {
//            return "Item " + (itemtt.getIndex() + 1);
//        }
//    };

    /**
     * Custom view holders for RecyclerView
     */
//    public final BindingRecyclerViewAdapter.ViewHolderFactory viewHolder = new BindingRecyclerViewAdapter.ViewHolderFactory() {
//        @Override
//        public RecyclerView.ViewHolder createViewHolder(ViewDataBinding binding) {
//            return new MyAwesomeViewHolder(binding.getRoot());
//        }
//    };

//    private static class MyAwesomeViewHolder extends RecyclerView.ViewHolder {
//        public MyAwesomeViewHolder(View itemView) {
//            super(itemView);
//        }
//    }
    public void addItem() {
        Bean item = new Bean();
        item.index = items.size();
        items.add(item);
    }

    public void removeItem() {
        if (items.size() > 1) {
            items.remove(items.size() - 1);
        }
    }
}
