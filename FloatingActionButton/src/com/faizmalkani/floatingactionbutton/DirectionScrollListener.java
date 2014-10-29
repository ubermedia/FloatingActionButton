/*
 * Copyright (c) 2014 SBG Apps
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

package com.faizmalkani.floatingactionbutton;

import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;

/**
 * Created by Stéphane on 09/07/2014.
 */
public class DirectionScrollListener implements AbsListView.OnScrollListener {

    private final int DIRECTION_CHANGE_THRESHOLD;
	private final FloatingActionButton mFloatingActionButton;
	private final boolean downToHide;
    private int mPrevPosition;
    private int mPrevTop;
    private boolean mUpdated;

    public DirectionScrollListener(FloatingActionButton floatingActionButton, boolean downToHide) {
        this.mFloatingActionButton = floatingActionButton;
        this.downToHide = downToHide;
        DIRECTION_CHANGE_THRESHOLD = ViewConfiguration.get(floatingActionButton.getContext()).getScaledOverflingDistance();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final View topChild = view.getChildAt(0);
        int firstViewTop = 0;
        if (topChild != null) {
            firstViewTop = topChild.getTop();
        }
        if (mPrevPosition == firstVisibleItem) {
            onScrolled(firstViewTop - mPrevTop);
        } else if (firstVisibleItem > mPrevPosition) {
            onScrolled(-DIRECTION_CHANGE_THRESHOLD - 1);
        } else {
            onScrolled(DIRECTION_CHANGE_THRESHOLD + 1);
        }
        mPrevPosition = firstVisibleItem;
        mPrevTop = firstViewTop;
    }

    public void onScrolled(int topDelta) {
        if (Math.abs(topDelta) > DIRECTION_CHANGE_THRESHOLD && mUpdated) {
            boolean goingDown = 0 > topDelta;
            boolean hide = !downToHide ^ goingDown;
            if (mFloatingActionButton.hide(hide)) {
                onShowTriggered(!hide);
            }
        }
        mUpdated = true;
    }

    protected void onShowTriggered(boolean showing) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}