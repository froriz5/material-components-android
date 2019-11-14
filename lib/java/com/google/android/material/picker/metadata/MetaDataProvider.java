package com.google.android.material.picker.metadata;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.picker.Month;

public interface MetaDataProvider<T extends View> extends Parcelable {

  @NonNull T getMetaData(@NonNull ViewGroup parent, @NonNull Month month, int dayOfMonth);

  void bindData(@NonNull T metaDataView, @NonNull Month month, int dayOfMonth);
}
