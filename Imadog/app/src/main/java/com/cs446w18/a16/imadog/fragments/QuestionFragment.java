package com.cs446w18.a16.imadog.fragments;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cs446w18.a16.imadog.Global;
import com.cs446w18.a16.imadog.R;
import com.cs446w18.a16.imadog.activities.menu.LobbyActivity;
import com.cs446w18.a16.imadog.model.GameConstants;

/**
 * Created by Jean-Baptiste on 18/02/2018.
 */

public class QuestionFragment extends SuperFragment {

    /* ----------------------------- ATTRIBUTES ----------------------------- */

    TextView questionLabel;

    EditText answerField;

    TextView timerLabel;


    /* ----------------------------- SETUP ----------------------------- */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_question, container, false);
        Bundle bundle = getArguments();

        // Set the question label
        questionLabel = view.findViewById(R.id.titleLabel);
        questionLabel.setText(bundle.getString("question"));

        // Answer field
        answerField = view.findViewById(R.id.answerField);
        answerField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        answerField.setTypeface(Global.fonts.get("OSSemibold"));

        // Answer field white background
        GradientDrawable background = (GradientDrawable)answerField.getBackground().getConstantState().newDrawable().mutate();;
        background.setColor(ContextCompat.getColor(getActivity(), R.color.white));
        answerField.setBackground(background);

        // Lock the view if player is dead
        if (Global.user.isDead()) {
            answerField.setEnabled(false);
        }

        // Set the return action (just hide UI)
        TextView.OnEditorActionListener fieldListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH ||
                        i == EditorInfo.IME_ACTION_DONE ||
                        keyEvent != null &&
                                keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    // When the user press enter
                    getGameActivity().hideSystemUI();
                }
                return false;

            }
        };
        answerField.setOnEditorActionListener(fieldListener);


        // Submit button
        Button submitButton = view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGameActivity().hideSystemUI();

                Toast submittedToast = Toast.makeText(getGameActivity(),
                        "Answer submitted.",
                        Toast.LENGTH_SHORT);
                submittedToast.show();

                getGameActivity().answeredQuestion(answerField.getText().toString());
            }
        });

        // Set the countdown timer
        long duration = GameConstants.questionPageDuration;
        timerLabel = view.findViewById(R.id.timerLabel);

        if (!Global.isCountDownStarted()) {
            setTimerLabel(duration);
            Global.timer = new CountDownTimer(duration, 1000) {
                @Override
                public void onTick(long l) {
                    Global.setCurrentTime(l);
                    setTimerLabel(l);
                }

                @Override
                public void onFinish() {
                    Global.countDownStopped();
                }
            }.start();
            Global.startTimer(duration);
        } else {
            setTimerLabel(Global.getCurrentTime());
        }



        return view;
    }


    /* ----------------------------- METHODS ----------------------------- */

    /// Updates the timer label with the given time
    private void setTimerLabel(long time) {
        int minutes = (int) time / 60000;
        int seconds = (int) time % 60000 / 1000;
        timerLabel.setText(minutes + ":" + String.format("%02d", seconds));
    }


}
