/*
 * Copyright 2018 Dionysios Karatzas
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

package eu.dkaratzas.starwarspedia.libs;

import android.animation.Animator;
import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import eu.dkaratzas.starwarspedia.R;
import eu.dkaratzas.starwarspedia.libs.animations.YoYo;
import eu.dkaratzas.starwarspedia.libs.animations.techniques.SlideInUpAnimator;
import eu.dkaratzas.starwarspedia.libs.animations.techniques.SlideOutDownAnimator;
import timber.log.Timber;

public class StatusMessage {
    private static volatile WeakReference<StatusMessage> sharedInstance;

    private static final int ANIMATION_DURATION = 300;
    private static int HIDE_DELAY = 5000;

    private WeakReference<View> mContainer;
    private WeakReference<TextView> mTextView;
    private Handler mHandler;

    private StatusMessage() {
        mHandler = new Handler();
    }

    public static void show(Activity activity, String message) {
        if (sharedInstance == null || sharedInstance.get() == null) {
            synchronized (StatusMessage.class) {
                sharedInstance = new WeakReference<>(new StatusMessage());

            }
        }
        sharedInstance.get().showMessage(activity, message, true);
    }

    public static void show(Activity activity, String message, boolean dismissible) {
        if (sharedInstance == null || sharedInstance.get() == null) {
            synchronized (StatusMessage.class) {
                sharedInstance = new WeakReference<>(new StatusMessage());

            }
        }
        sharedInstance.get().showMessage(activity, message, dismissible);
    }

    public static void hide() {
        if (sharedInstance != null && sharedInstance.get() != null) {
            sharedInstance.get().hideMessage();
        }
    }

    private void init(Activity activity, boolean dismissible) {
        // Remove handler callbacks
        mHandler.removeCallbacksAndMessages(null);
        // remove the view if already exists
        removeView();

        // inflate status layout view
        ViewGroup container = activity.findViewById(android.R.id.content);
        View view = activity.getLayoutInflater().inflate(R.layout.status_layout, container);

        mContainer = new WeakReference<>(view.findViewById(R.id.statusMessageContainer));
        mContainer.get().setVisibility(View.GONE);

        if (dismissible) {

            mContainer.get().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            });

        }

        mTextView = new WeakReference<>((TextView) view.findViewById(R.id.tvStatusMessage));

    }

    private void showMessage(Activity activity, String message, boolean dismissible) {
        init(activity, dismissible);

        mTextView.get().setText(message);

        YoYo.with(new SlideInUpAnimator())
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        mContainer.get().setVisibility(View.VISIBLE);
                    }
                })
                .duration(ANIMATION_DURATION)
                .playOn(mContainer.get());

        if (dismissible) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideMessage();
                }
            }, HIDE_DELAY);
        }
    }

    private void hideMessage() {
        if (mContainer != null && mContainer.get() != null && mContainer.get().getParent() != null) {
            YoYo.with(new SlideOutDownAnimator())
                    .duration(ANIMATION_DURATION)
                    .onEnd(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            removeView();
                        }
                    })
                    .playOn(mContainer.get());
        }
    }

    private void removeView() {
        if (mContainer != null && mContainer.get() != null && mContainer.get().getParent() != null) {
            ((ViewGroup) mContainer.get().getParent()).removeView(mContainer.get());
            mContainer = null;
            Timber.d("Removed status message container");
        }
    }
}
