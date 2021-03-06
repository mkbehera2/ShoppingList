package com.jshvarts.shoppinglist.lobby.fragments;

import android.arch.lifecycle.ViewModel;

import com.google.common.base.Strings;
import com.jshvarts.shoppinglist.common.domain.model.DatabaseConstants;
import com.jshvarts.shoppinglist.common.domain.model.ShoppingList;
import com.jshvarts.shoppinglist.common.viewmodel.SingleLiveEvent;
import com.jshvarts.shoppinglist.rx.SchedulersFacade;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

class AddShoppingListItemViewModel extends ViewModel {

    private final LoadShoppingListUseCase loadShoppingListUseCase;

    private final AddShoppingListItemUseCase addShoppingListItemUseCase;

    private final SchedulersFacade schedulersFacade;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private SingleLiveEvent<Boolean> itemAdded = new SingleLiveEvent<>();

    private SingleLiveEvent<Void> itemInvalid = new SingleLiveEvent<>();

    private SingleLiveEvent<Void> hideKeyboard = new SingleLiveEvent<>();

    AddShoppingListItemViewModel(AddShoppingListItemUseCase addShoppingListItemUseCase,
                                 LoadShoppingListUseCase loadShoppingListUseCase,
                                 SchedulersFacade schedulersFacade) {
        this.addShoppingListItemUseCase = addShoppingListItemUseCase;
        this.loadShoppingListUseCase = loadShoppingListUseCase;
        this.schedulersFacade = schedulersFacade;
    }

    SingleLiveEvent<Boolean> itemAdded() {
        return itemAdded;
    }

    SingleLiveEvent<Void> itemInvalid() {
        return itemInvalid;
    }

    SingleLiveEvent<Void> hideKeyboard() {
        return hideKeyboard;
    }

    void addShoppingListItem(String shoppingListItemName) {
        hideKeyboard.call();

        if (Strings.isNullOrEmpty(shoppingListItemName)) {
            itemInvalid.call();
            return;
        }
        loadShoppingListAndAddItem(shoppingListItemName);
    }

    private void loadShoppingListAndAddItem(String shoppingListItemName) {
        disposables.add(loadShoppingListUseCase.loadShoppingList(DatabaseConstants.DEFAULT_SHOPPING_LIST_ID)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .firstElement()
                .subscribe(shoppingList -> addShoppingListItem(shoppingList, shoppingListItemName),
                        throwable -> Timber.e(throwable))
        );
    }

    private void addShoppingListItem(ShoppingList shoppingList, String shoppingListItemName) {
        disposables.add(addShoppingListItemUseCase.execute(shoppingList, shoppingListItemName)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(updatedShoppingList -> itemAdded.setValue(true), throwable -> {
                    Timber.e(throwable);
                    itemAdded.setValue(false);
                }));
    }
}
