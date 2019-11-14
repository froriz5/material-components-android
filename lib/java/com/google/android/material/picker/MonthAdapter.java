/*
 * Copyright 2019 The Android Open Source Project
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
package com.google.android.material.picker;

import com.google.android.material.R;
import com.google.android.material.picker.metadata.MetaDataProvider;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;

/**
 * Represents the days of a month with {@link TextView} instances for each day.
 *
 * <p>The number of rows is always equal to the maximum number of weeks that can exist across all
 * months (e.g., 6 for the GregorianCalendar).
 */
class MonthAdapter extends BaseAdapter {

  /**
   * The maximum number of weeks possible in any month. 6 for {@link java.util.GregorianCalendar}.
   */
  static final int MAXIMUM_WEEKS = Calendar.getInstance().getMaximum(Calendar.WEEK_OF_MONTH);

  final Month month;
  /**
   * The {@link DateSelector} dictating the draw behavior of {@link #getView(int, View, ViewGroup)}.
   */
  final DateSelector<?> dateSelector;

  CalendarStyle calendarStyle;
  final CalendarConstraints calendarConstraints;

  @Nullable MetaDataProvider metaDataProvider;

  MonthAdapter(Month month, DateSelector<?> dateSelector, CalendarConstraints calendarConstraints, @Nullable MetaDataProvider metaDataProvider) {
    this.month = month;
    this.dateSelector = dateSelector;
    this.calendarConstraints = calendarConstraints;
    this.metaDataProvider = metaDataProvider;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  /**
   * Returns a {@link Long} object for the given grid position
   *
   * @param position Index for the item. 0 matches the {@link Calendar#getFirstDayOfWeek()} for the
   *     first week of the month represented by {@link Month}.
   * @return A {@link Long} representing the day at the position or null if the position does not
   *     represent a valid day in the month.
   */
  @Nullable
  @Override
  public Long getItem(int position) {
    if (position < month.daysFromStartOfWeekToFirstOfMonth() || position > lastPositionInMonth()) {
      return null;
    }
    return month.getDay(positionToDay(position));
  }

  @Override
  public long getItemId(int position) {
    return position / month.daysInWeek;
  }

  /**
   * Returns the number of days in a month plus the amount required to off-set for the first day to
   * the correct position within the week.
   *
   * <p>{@see MonthAdapter#firstPositionInMonth}.
   *
   * @return The maximum valid position index
   */
  @Override
  public int getCount() {
    return month.daysInMonth + firstPositionInMonth();
  }

  @Override
  public LinearLayout getView(int position, View convertView, ViewGroup parent) {
    initializeStyles(parent.getContext());
    LinearLayout dayContainer = (LinearLayout) convertView;
//    TextView day = (TextView) convertView;

    if (convertView == null) {
      LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
//      day = (TextView) layoutInflater.inflate(R.layout.mtrl_calendar_day, parent, false);
      dayContainer = (LinearLayout) layoutInflater.inflate(R.layout.mtrl_calendar_day, parent, false);
    }

    TextView day = dayContainer.findViewById(R.id.day_view);

    int offsetPosition = position - firstPositionInMonth();
    if (offsetPosition < 0 || offsetPosition >= month.daysInMonth) {
//      day.setVisibility(View.GONE);
//      day.setEnabled(false);
      dayContainer.setVisibility(View.GONE);
      dayContainer.setEnabled(false);
    } else {
      int dayNumber = offsetPosition + 1;
      // The tag and text uniquely identify the view within the MaterialCalendar for testing
      day.setTag(month);
      day.setText(String.valueOf(dayNumber));
      long dayInMillis = month.getDay(dayNumber);
      if (month.year == Month.today().year) {
        day.setContentDescription(DateStrings.getMonthDayOfWeekDay(dayInMillis));
      } else {
        day.setContentDescription(DateStrings.getYearMonthDayOfWeekDay(dayInMillis));
      }
//      day.setVisibility(View.VISIBLE);
//      day.setEnabled(true);
      dayContainer.setVisibility(View.VISIBLE);
      dayContainer.setEnabled(true);

      if (metaDataProvider != null) {
        boolean isAttached = dayContainer.getChildCount() > 1;
        View metaDataContainerView;
        if (isAttached) {
          metaDataContainerView = dayContainer.getChildAt(1);
        } else {
          metaDataContainerView = metaDataProvider.getMetaData(parent, month, dayNumber);
          dayContainer.addView(metaDataContainerView);
        }

//        if (metaDataContainerView == null) {
//          dayContainer.getChildAt(1).setVisibility(View.INVISIBLE);
//        }

//        if (!isAttached) {
////          dayContainer.addView(metaDataContainerView);
//          metaDataProvider.bindData(metaDataContainerView, month, dayNumber);
//        } else {
////          metaDataContainerView = dayContainer.getChildAt(1);
//          metaDataProvider.bindData(metaDataContainerView, month, dayNumber);
//        }
        metaDataProvider.bindData(metaDataContainerView, month, dayNumber);
      }
    }

    Long date = getItem(position);
    if (date != null) {
      if (calendarConstraints.getDateValidator().isValid(date)) {
//        day.setEnabled(true);
        dayContainer.setEnabled(true);
        if (dateSelector.getSelectedDays().contains(date)) {
          calendarStyle.selectedDay.styleItem(day);
        } else if (DateUtils.isToday(date)) {
          calendarStyle.todayDay.styleItem(day);
        } else {
          calendarStyle.day.styleItem(day);
        }
      } else {
//        day.setEnabled(false);
        dayContainer.setEnabled(false);
        calendarStyle.invalidDay.styleItem(day);
      }
    }
//    return day;
    return dayContainer;
  }

  private void initializeStyles(Context context) {
    if (calendarStyle == null) {
      calendarStyle = new CalendarStyle(context);
    }
  }

  /**
   * Returns the index of the first position which is part of the month.
   *
   * <p>For example, this returns the position index representing February 1st. Since position 0
   * represents a day which must be the first day of the week, the first position in the month may
   * be greater than 0.
   */
  int firstPositionInMonth() {
    return month.daysFromStartOfWeekToFirstOfMonth();
  }

  /**
   * Returns the index of the last position which is part of the month.
   *
   * <p>For example, this returns the position index representing November 30th. Since position 0
   * represents a day which must be the first day of the week, the last position in the month may
   * not match the number of days in the month.
   */
  int lastPositionInMonth() {
    return month.daysFromStartOfWeekToFirstOfMonth() + month.daysInMonth - 1;
  }

  /**
   * Returns the day representing the provided adapter index
   *
   * @param position The adapter index
   * @return The day corresponding to the adapter index. May be non-positive for position inputs
   *     less than {@link MonthAdapter#firstPositionInMonth()}.
   */
  int positionToDay(int position) {
    return position - month.daysFromStartOfWeekToFirstOfMonth() + 1;
  }

  /** Returns the adapter index representing the provided day. */
  int dayToPosition(int day) {
    int offsetFromFirst = day - 1;
    return firstPositionInMonth() + offsetFromFirst;
  }

  /** True when a provided adapter position is within the calendar month */
  boolean withinMonth(int position) {
    return position >= firstPositionInMonth() && position <= lastPositionInMonth();
  }

  /**
   * True when the provided adapter position is the smallest position for a value of {@link
   * MonthAdapter#getItemId(int)}.
   */
  boolean isFirstInRow(int position) {
    return position % month.daysInWeek == 0;
  }

  /**
   * True when the provided adapter position is the largest position for a value of {@link
   * MonthAdapter#getItemId(int)}.
   */
  boolean isLastInRow(int position) {
    return (position + 1) % month.daysInWeek == 0;
  }
}
