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
package com.android.internal.os;

import android.annotation.Nullable;
import android.os.SystemClock;
import android.util.SparseLongArray;

/**
 * uid: user_time_micro_seconds system_time_micro_seconds power_in_milli-amp-micro_seconds
 *
 * This provides the time a UID's processes spent executing in user-space and kernel-space.
 * The file contains a monotonically increasing count of time for a single boot. This class
 * maintains the previous results of a call to {@link #readDelta} in order to provide a proper
 * delta.
 */
public class KernelUidCpuTimeReader {

    /**
     * Callback interface for processing each line of the proc file.
     */
    public interface Callback {
        /**
         * @param uid UID of the app
         * @param userTimeUs time spent executing in user space in microseconds
         * @param systemTimeUs time spent executing in kernel space in microseconds
         * @param powerMaUs power consumed executing, in milli-ampere microseconds
         */
        void onUidCpuTime(int uid, long userTimeUs, long systemTimeUs, long powerMaUs);
    }

    private SparseLongArray mLastUserTimeUs = new SparseLongArray();
    private SparseLongArray mLastSystemTimeUs = new SparseLongArray();
    private SparseLongArray mLastPowerMaUs = new SparseLongArray();
    private long mLastTimeReadUs = 0;

    /**
     * Reads the proc file, calling into the callback with a delta of time for each UID.
     * @param callback The callback to invoke for each line of the proc file. If null,
     *                 the data is consumed and subsequent calls to readDelta will provide
     *                 a fresh delta.
     */
    public void readDelta(@Nullable Callback callback) {
        long nowUs = SystemClock.elapsedRealtime() * 1000;
        mLastTimeReadUs = nowUs;
    }

    /**
     * Removes the UID from the kernel module and from internal accounting data.
     * @param uid The UID to remove.
     */
    public void removeUid(int uid) {
        int index = mLastUserTimeUs.indexOfKey(uid);
        if (index >= 0) {
            mLastUserTimeUs.removeAt(index);
            mLastSystemTimeUs.removeAt(index);
            mLastPowerMaUs.removeAt(index);
        }
    }
}
