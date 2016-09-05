package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;

/**
 * Created by yale on 15/9/15.
 */
public class QACell extends LinearLayout {
    private TextView mQuestionContentLabel;
    private TextView mAnswerContentLabel;

    public QACell(Context context) {
        this(context, null);
    }

    public QACell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QACell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);

        inflate(context, R.layout.cell_qa, this);

        {
            View topLine = new View(context);
            topLine.setBackgroundResource(R.color.gmf_border_line);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, 1);
            addView(topLine, 0, params);
        }

        {
            View bottomLine = new View(context);
            bottomLine.setBackgroundResource(R.color.gmf_border_line);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, 1);
            addView(bottomLine, params);
        }

        // bind child views
        mQuestionContentLabel = v_findView(this, R.id.label_question_content);
        mAnswerContentLabel = v_findView(this, R.id.label_answer_content);

        // init properties
        String questionText = null;
        String answerText = null;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QACell, defStyleAttr, 0);
        if (a != null) {
            questionText = a.getString(R.styleable.QACell_question);
            answerText = a.getString(R.styleable.QACell_answer);
            a.recycle();
        }

        // init child views
        mQuestionContentLabel.setText(questionText);
        mAnswerContentLabel.setText(answerText);
    }

    public void setQuestion(CharSequence text) {
        mQuestionContentLabel.setText(text);
    }

    public void setAnswer(CharSequence text) {
        mAnswerContentLabel.setText(text);
    }
}
