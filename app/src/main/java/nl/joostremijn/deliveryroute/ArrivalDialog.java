package nl.joostremijn.deliveryroute;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ArrivalDialog extends Dialog {
    public ArrivalDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.arrival_dialog);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        View view = getWindow().getDecorView();
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.x = 10;
        lp.y = 10;
        lp.width = 300;
        lp.height = 300;
        getWindow().setAttributes(lp);
        //getWindowManager().updateViewLayout(view, lp);
    }
}
