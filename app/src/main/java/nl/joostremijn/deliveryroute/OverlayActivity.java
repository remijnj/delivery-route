package nl.joostremijn.deliveryroute;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class OverlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay);
    }
}
