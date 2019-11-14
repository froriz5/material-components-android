/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.material.catalog.picker;

import io.material.catalog.R;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.picker.Month;
import com.google.android.material.picker.metadata.MetaDataProvider;
import com.google.android.material.snackbar.Snackbar;

import android.os.Parcel;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.picker.MaterialDatePicker;
import io.material.catalog.feature.DemoFragment;
import java.util.Calendar;

/** A fragment that displays the main Picker demos for the Catalog app. */
public class PickerMainDemoFragment extends DemoFragment {

  private Snackbar snackbar;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.picker_main_demo, viewGroup, false);
    LinearLayout dialogLaunchersLayout = view.findViewById(R.id.picker_launcher_buttons_layout);

    snackbar = Snackbar.make(viewGroup, R.string.cat_picker_no_action, Snackbar.LENGTH_LONG);
    int dialogTheme = resolveOrThrow(getContext(), R.attr.materialCalendarTheme);
    int fullscreenTheme = resolveOrThrow(getContext(), R.attr.materialCalendarFullscreenTheme);

    LayoutInflater inflater = requireActivity().getLayoutInflater();

    MetaDataProvider<View> metaDataProvider = new MetaDataProvider<View>() {
      @Override
      public int describeContents() {
        return 0;
      }

      @Override
      public void writeToParcel(Parcel dest, int flags) {

      }

      @NonNull
      @Override
      public View getMetaData(@NonNull ViewGroup parent, @NonNull Month month, int dayOfMonth) {
        return inflater.inflate(R.layout.mtrl_calendar_day_meta_data, parent, false);
      }

      @Override
      public void bindData(@NonNull View metaDataView, @NonNull Month month, int dayOfMonth) {
        if (dayOfMonth % 2 == 0) {
          metaDataView.setVisibility(View.VISIBLE);

          TextView metaDataTextView = metaDataView.findViewById(R.id.day_meta_data_view);
          ImageView metaDataIconView = metaDataView.findViewById(R.id.day_meta_data_icon_view);

          metaDataTextView.setText("\uD83D\uDE0A");
          metaDataIconView.setImageResource(R.drawable.asld_clock_clock);
        } else {
          metaDataView.setVisibility(View.INVISIBLE);
        }
      }
    };

//    setupDialogFragment(
//        dialogLaunchersLayout,
//        R.string.cat_picker_date_calendar,
//        MaterialDatePicker.Builder.datePicker());
    setupDialogFragment(
        dialogLaunchersLayout,
        R.string.cat_picker_date_calendar,
        MaterialDatePicker.Builder.datePicker().setMetaDataProvider(metaDataProvider));
    setupDialogFragment(
        dialogLaunchersLayout,
        R.string.cat_picker_date_calendar_fullscreen,
        MaterialDatePicker.Builder.datePicker().setTheme(fullscreenTheme));

    setupDialogFragment(
        dialogLaunchersLayout,
        R.string.cat_picker_date_range_calendar,
        MaterialDatePicker.Builder.dateRangePicker());
    setupDialogFragment(
        dialogLaunchersLayout,
        R.string.cat_picker_date_range_calendar_dialog,
        MaterialDatePicker.Builder.dateRangePicker().setTheme(dialogTheme));

    layoutInflater.inflate(R.layout.cat_picker_spacer, dialogLaunchersLayout, true);
    addDialogLauncher(
        dialogLaunchersLayout, R.string.cat_picker_base, v -> frameworkTodayDatePicker(0).show());

    return view;
  }

  private static int resolveOrThrow(Context context, @AttrRes int attributeResId) {
    TypedValue typedValue = new TypedValue();
    if (context.getTheme().resolveAttribute(attributeResId, typedValue, true)) {
      return typedValue.data;
    }
    throw new IllegalArgumentException(context.getResources().getResourceName(attributeResId));
  }

  private void setupDialogFragment(
      ViewGroup dialogLaunchersLayout,
      @StringRes int tagId,
      MaterialDatePicker.Builder<?> builder) {
    String tag = getString(tagId);
    final MaterialDatePicker<?> materialCalendarPicker;
    if (getFragmentManager().findFragmentByTag(tag) == null) {
      materialCalendarPicker = builder.build();
    } else {
      materialCalendarPicker = (MaterialDatePicker<?>) getFragmentManager().findFragmentByTag(tag);
    }
    addSnackBarListeners(materialCalendarPicker);
    addDialogLauncher(
        dialogLaunchersLayout, tagId, v -> materialCalendarPicker.show(getFragmentManager(), tag));
  }

  private void addSnackBarListeners(MaterialDatePicker<?> materialCalendarPicker) {
    materialCalendarPicker.addOnPositiveButtonClickListener(
        selection -> {
          snackbar.setText(materialCalendarPicker.getHeaderText());
          snackbar.show();
        });
    materialCalendarPicker.addOnNegativeButtonClickListener(
        dialog -> {
          snackbar.setText(R.string.cat_picker_user_clicked_cancel);
          snackbar.show();
        });
    materialCalendarPicker.addOnCancelListener(
        dialog -> {
          snackbar.setText(R.string.cat_picker_cancel);
          snackbar.show();
        });
  }

  private void addDialogLauncher(
      ViewGroup viewGroup, @StringRes int stringResId, OnClickListener onClickListener) {
    MaterialButton dialogLauncherButton = new MaterialButton(viewGroup.getContext());
    dialogLauncherButton.setOnClickListener(onClickListener);
    dialogLauncherButton.setText(stringResId);
    viewGroup.addView(dialogLauncherButton);
  }

  private DatePickerDialog frameworkTodayDatePicker(@StyleRes int themeResId) {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    return new DatePickerDialog(getContext(), themeResId, null, year, month, day);
  }
}
