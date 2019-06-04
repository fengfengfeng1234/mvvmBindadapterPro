/*
 * Copyright 2016 drakeet. https://github.com/drakeet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.tatarka.bindingcollectionadapter2.multitype;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemVM;


/**
 */
class OneToManyBuilder<T> implements OneToManyFlow<T>, OneToManyEndpoint<T> {


    private final @NonNull
    Class<? extends T> clazz;
    private ItemVM<T, ?>[] binders;
    MultiTypePool multiTypePool;

    OneToManyBuilder(MultiTypePool multiTypePool, @NonNull Class<? extends T> clazz) {
        this.clazz = clazz;
        this.multiTypePool = multiTypePool;

    }


    @Override
    @CheckResult
    @SafeVarargs
    public final @NonNull
    OneToManyEndpoint<T> to(@NonNull ItemVM<T, ?>... binders) {
        this.binders = binders;
        return this;
    }


    @Override
    public void withLinker(@NonNull Linker<T> linker) {
        doRegister(linker);
    }


    @Override
    public void withClassLinker(@NonNull ClassLinker<T> classLinker) {
        /**
         *ClassLinkerWrapper.wrap
         *  继承同一个抽象方法.
         *  具体处理查找操作
         */
        doRegister(ClassLinkerWrapper.wrap(classLinker, binders));
    }


    /**
     * 一对多情况 进行注册.
     */
    private void doRegister(@NonNull Linker<T> linker) {
        for (ItemVM<T, ?> binder : binders) {
            multiTypePool.register(clazz, binder, linker);
        }
    }
}
