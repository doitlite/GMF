package yale.extension.common.animation;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.goldmf.GMFund.base.KeepClassProtocol;

/**
 * Created by yalez on 2016/7/13.
 */
public class ViewAnimationProxy implements KeepClassProtocol {
    private View target;
    private ViewGroup.LayoutParams params;
    private ViewGroup.MarginLayoutParams marginParams;

    public ViewAnimationProxy(View target) {
        this.target = target;
        this.params = target.getLayoutParams();
        this.marginParams = this.params instanceof ViewGroup.MarginLayoutParams ? (ViewGroup.MarginLayoutParams) this.params : null;
    }

    public void setLeftMargin(int leftMargin) {
        marginParams.leftMargin = leftMargin;
        target.requestLayout();
    }

    public int getLeftMargin() {
        return marginParams.leftMargin;
    }

    public void setTopMargin(int topMargin) {
        marginParams.topMargin = topMargin;
        target.requestLayout();
    }

    public int getTopMargin() {
        return marginParams.topMargin;
    }

    public void setWidth(int width) {
        params.width = width;
        target.requestLayout();
    }

    public int getWidth() {
        return params.width;
    }

    public void setHeight(int height) {
        params.height = height;
        target.requestLayout();
    }

    public int getHeight() {
        return params.height;
    }

    public void setTranslationX(float translationX) {
        target.setTranslationX(translationX);
    }

    public float getTranslationX() {
        return target.getTranslationX();
    }

    public void setTranslationY(float translationY) {
        target.setTranslationY(translationY);
    }

    public float getTranslationY() {
        return target.getTranslationY();
    }
}
