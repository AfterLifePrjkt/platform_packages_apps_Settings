/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.datausage;

import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.utils.StringUtil;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Provides a summary of data usage.
 */
public class DataUsageSummaryPreference extends Preference {
    private static final long MILLIS_IN_A_DAY = TimeUnit.DAYS.toMillis(1);
    private static final long WARNING_AGE = TimeUnit.HOURS.toMillis(6L);

    private boolean mChartEnabled = true;
    private String mStartLabel;
    private String mEndLabel;

    /** large vs small size is 36/16 ~ 2.25 */
    private static final float LARGER_FONT_RATIO = 2.25f;
    private static final float SMALLER_FONT_RATIO = 1.0f;

    private boolean mDefaultTextColorSet;
    private int mDefaultTextColor;
    private int mNumPlans;
    /** The ending time of the billing cycle in milliseconds since epoch. */
    private long mCycleEndTimeMs;
    /** The time of the last update in standard milliseconds since the epoch */
    private long mSnapshotTimeMs;
    /** Name of carrier, or null if not available */
    private CharSequence mCarrierName;
    private String mLimitInfoText;
    private Intent mLaunchIntent;

    /** Progress to display on ProgressBar */
    private float mProgress;
    private boolean mHasMobileData;

    /**
     * The size of the first registered plan if one exists or the size of the warning if it is set.
     * -1 if no information is available.
     */
    private long mDataplanSize;

    /** The number of bytes used since the start of the cycle. */
    private long mDataplanUse;

    public DataUsageSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.data_usage_summary_preference);
    }

    public void setLimitInfo(String text) {
        if (!Objects.equals(text, mLimitInfoText)) {
            mLimitInfoText = text;
            notifyChanged();
        }
    }

    public void setProgress(float progress) {
        mProgress = progress;
        notifyChanged();
    }

    public void setUsageInfo(long cycleEnd, long snapshotTime, CharSequence carrierName,
            int numPlans, Intent launchIntent) {
        mCycleEndTimeMs = cycleEnd;
        mSnapshotTimeMs = snapshotTime;
        mCarrierName = carrierName;
        mNumPlans = numPlans;
        mLaunchIntent = launchIntent;
        notifyChanged();
    }

    public void setChartEnabled(boolean enabled) {
        if (mChartEnabled != enabled) {
            mChartEnabled = enabled;
            notifyChanged();
        }
    }

    public void setLabels(String start, String end) {
        mStartLabel = start;
        mEndLabel = end;
        notifyChanged();
    }

    void setUsageNumbers(long used, long dataPlanSize, boolean hasMobileData) {
        mDataplanUse = used;
        mDataplanSize = dataPlanSize;
        mHasMobileData = hasMobileData;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);


        if (mChartEnabled && (!TextUtils.isEmpty(mStartLabel) || !TextUtils.isEmpty(mEndLabel))) {
            holder.findViewById(R.id.label_bar).setVisibility(View.VISIBLE);
            ProgressBar bar = (ProgressBar) holder.findViewById(R.id.determinateBar);
            bar.setProgress((int) (mProgress * 100));
            ((TextView) holder.findViewById(android.R.id.text1)).setText(mStartLabel);
            ((TextView) holder.findViewById(android.R.id.text2)).setText(mEndLabel);
        } else {
            holder.findViewById(R.id.label_bar).setVisibility(View.GONE);
        }

        updateDataUsageLabels(holder);

        TextView usageTitle = (TextView) holder.findViewById(R.id.usage_title);
        usageTitle.setVisibility(mNumPlans > 1 ? View.VISIBLE : View.GONE);

        updateCycleTimeText(holder);


        updateCarrierInfo((TextView) holder.findViewById(R.id.carrier_and_update));

        Button launchButton = (Button) holder.findViewById(R.id.launch_mdp_app_button);
        launchButton.setOnClickListener((view) -> {
            getContext().sendBroadcast(mLaunchIntent);
        });
        if (mLaunchIntent != null) {
            launchButton.setVisibility(View.VISIBLE);
        } else {
            launchButton.setVisibility(View.GONE);
        }

        TextView limitInfo = (TextView) holder.findViewById(R.id.data_limits);
        limitInfo.setVisibility(
                mLimitInfoText == null || mLimitInfoText.isEmpty() ? View.GONE : View.VISIBLE);
        limitInfo.setText(mLimitInfoText);
    }


    private void updateDataUsageLabels(PreferenceViewHolder holder) {
        TextView usageNumberField = (TextView) holder.findViewById(R.id.data_usage_view);

        final Formatter.BytesResult usedResult = Formatter.formatBytes(getContext().getResources(),
                mDataplanUse, Formatter.FLAG_CALCULATE_ROUNDED);
        final SpannableString usageNumberText = new SpannableString(usedResult.value);
        final int textSize =
                getContext().getResources().getDimensionPixelSize(R.dimen.usage_number_text_size);
        usageNumberText.setSpan(new AbsoluteSizeSpan(textSize), 0, usageNumberText.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        CharSequence template = getContext().getText(R.string.data_used_formatted);

        CharSequence usageText =
                TextUtils.expandTemplate(template, usageNumberText, usedResult.units);
        usageNumberField.setText(usageText);

        if (mHasMobileData && mNumPlans >= 0 && mDataplanSize > 0L) {
            TextView usageRemainingField = (TextView) holder.findViewById(R.id.data_remaining_view);
            long dataRemaining = mDataplanSize - mDataplanUse;
            if (dataRemaining >= 0) {
                usageRemainingField.setText(
                        TextUtils.expandTemplate(getContext().getText(R.string.data_remaining),
                                Formatter.formatFileSize(getContext(), dataRemaining)));
            } else {
                usageRemainingField.setText(
                        TextUtils.expandTemplate(getContext().getText(R.string.data_overusage),
                                Formatter.formatFileSize(getContext(), -dataRemaining)));
            }
        }
    }

    private void updateCycleTimeText(PreferenceViewHolder holder) {
        TextView cycleTime = (TextView) holder.findViewById(R.id.cycle_left_time);

        long millisLeft = mCycleEndTimeMs - System.currentTimeMillis();
        if (millisLeft <= 0) {
            cycleTime.setText(getContext().getString(R.string.billing_cycle_none_left));
        } else {
            int daysLeft = (int)(millisLeft / MILLIS_IN_A_DAY);
            cycleTime.setText(daysLeft < 1
                            ? getContext().getString(R.string.billing_cycle_less_than_one_day_left)
                            : getContext().getResources().getQuantityString(
                            R.plurals.billing_cycle_days_left, daysLeft, daysLeft));
        }
    }


    private void updateCarrierInfo(TextView carrierInfo) {
        if (mNumPlans > 0 && mSnapshotTimeMs >= 0L) {
            long updateAge = System.currentTimeMillis() - mSnapshotTimeMs;
            carrierInfo.setVisibility(View.VISIBLE);
            if (mCarrierName != null) {
                carrierInfo.setText(getContext().getString(R.string.carrier_and_update_text,
                        mCarrierName, StringUtil.formatRelativeTime(
                                getContext(), updateAge, false /* withSeconds */)));
            } else {
                carrierInfo.setText(getContext().getString(R.string.no_carrier_update_text,
                        StringUtil.formatRelativeTime(
                                getContext(), updateAge, false /* withSeconds */)));
            }

            carrierInfo.setTextColor(
                    updateAge <= WARNING_AGE
                    ? Utils.getColorAttr(getContext(), android.R.attr.textColorPrimary)
                    : Utils.getColorAttr(getContext(), android.R.attr.colorError));
        } else {
            carrierInfo.setVisibility(View.GONE);
        }
    }
}
