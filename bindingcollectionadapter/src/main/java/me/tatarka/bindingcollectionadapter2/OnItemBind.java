package me.tatarka.bindingcollectionadapter2;

/**
 * Callback for setting up a {@link ItemBinding} for an itemtt in the collection.
 *
 * @param <T> the itemtt type
 */
public interface OnItemBind<T> {
    /**
     * Called on each itemtt in the collection, allowing you to modify the given {@link ItemBinding}.
     * Note that you should not do complex processing in this method as it's called many times.
     */
    void onItemBind(ItemBinding itemBinding, int position, T item);
}
