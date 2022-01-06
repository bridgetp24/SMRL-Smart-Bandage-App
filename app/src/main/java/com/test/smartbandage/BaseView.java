package com.test.smartbandage;


/**
 * TODO: Add a class header comment!
 */

public interface BaseView<T extends BasePresenter> {

    void setPresenter(T presenter);
}
