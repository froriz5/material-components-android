/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.bottomsheet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.R;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatDialogFragment;

/**
 * Modal bottom sheet. This is a version of {@link DialogFragment} that shows a bottom sheet using
 * {@link BottomSheetDialog} instead of a floating dialog.
 */
public class BottomSheetDialogFragment extends AppCompatDialogFragment {

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new BottomSheetDialog(getContext(), getTheme());
  }

  @Override
  public void dismiss() {
    Dialog dialog = getDialog();
    if (dialog != null) {
      View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
      if (bottomSheet != null) {
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
        if (behavior.dismissWithAnimation) {
          dismissWithAnimation(behavior);
        } else {
          super.dismiss();
        }
      } else {
        super.dismiss();
      }
    } else {
      super.dismiss();
    }
  }

  @Override
  public void dismissAllowingStateLoss() {
    Dialog dialog = getDialog();
    if (dialog != null) {
      View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
      if (bottomSheet != null) {
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

        if (behavior.dismissWithAnimation) {
          dismissWithAnimation(behavior, true);
        } else {
          super.dismissAllowingStateLoss();
        }
      } else {
        super.dismissAllowingStateLoss();
      }
    } else {
      super.dismissAllowingStateLoss();
    }
  }

  private void dismissWithAnimation(@NonNull BottomSheetBehavior behavior) {
    dismissWithAnimation(behavior, false);
  }

  private void dismissWithAnimation(@NonNull BottomSheetBehavior behavior,
                                    boolean allowingStateLoss) {
    if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
      if (allowingStateLoss) {
        super.dismissAllowingStateLoss();
      } else {
        super.dismiss();
      }
    } else {
      BottomSheetBehavior.BottomSheetCallback dismissCallback =
          getDismissBehaviorCallback(allowingStateLoss);

      behavior.setBottomSheetCallback(dismissCallback);

      behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
  }

  private BottomSheetBehavior.BottomSheetCallback getDismissBehaviorCallback(
      final boolean allowingStateLoss) {

    final WeakReference<BottomSheetDialogFragment> dialogFragmentWeakReference =
        new WeakReference<>(this);

    return new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
          BottomSheetDialogFragment dialogFragment = dialogFragmentWeakReference.get();

          if (dialogFragment != null) {
            if (allowingStateLoss) {
              dialogFragment.dismissAllowingStateLoss();
            } else {
              dialogFragment.dismiss();
            }
          }
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
    };
  }
}
