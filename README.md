# mvvm_bindadapter_pro



改项目主要是为了解决一个问题。

```
public final OnItemBindClass<Object> multipleItems = new OnItemBindClass<>()
            .map(String.class, BR.item, R.layout.item_header_footer)
            .map(ItemViewModel.class, BR.item, R.layout.item);
```

* * *
ItemViewModel  我还要把我的 网络请求返回 list集合 取出每一个对象，在生成 ItemViewModel  在我使用的时候 我觉得特别麻烦。
* * *
改：
=============
```
public abstract class ItemVM<T,VM extends ViewDataBinding> extends ViewModel {

    protected abstract void onBindViewHolder(@NonNull VM viewDataBinding, @NonNull T item);

    protected abstract  int getVariableId();

    protected abstract  int getLayout();

    protected   int position;

}

通过注册形式 不断调用  ItemView 抽象方法

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


```




参考：

https://github.com/evant/binding-collection-adapter

https://github.com/drakeet/MultiType