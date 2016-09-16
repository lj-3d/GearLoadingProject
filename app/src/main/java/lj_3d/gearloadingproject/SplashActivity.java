package lj_3d.gearloadingproject;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import lj_3d.gearloadinglayout.enums.ShowMode;
import lj_3d.gearloadinglayout.enums.Style;
import lj_3d.gearloadinglayout.gearViews.ThreeGearsLayout;
import lj_3d.gearloadinglayout.utils.GearDialogBuilder;

/**
 * Created by liubomyr on 16.09.16.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final Resources mResources = getResources();
        GearDialogBuilder.getInstance(this)
                .setType(ThreeGearsLayout.class)
                .setShowDialogDuration(200)
                .setShowMode(ShowMode.CENTER)
                .setDuration(3000)
                .blurBackground(true, 4, 4f)
                .enableCutLayout(true)
                .setCutLayoutAlpha(0.35f)
                .setStyle(Style.DIALOG)
                .setDialogBackgroundColor(Color.TRANSPARENT)
                .setMainBackgroundColor(Color.TRANSPARENT)
                .setShadowWidth(getResources().getDimensionPixelSize(R.dimen.shadow_width))
                .setCutLayoutColor(mResources.getColor(android.R.color.white))
                .setFirstGearColor(mResources.getColor(android.R.color.white))
                .setSecondGearColor(mResources.getColor(android.R.color.white))
                .setThirdGearColor(mResources.getColor(android.R.color.white))
                .show();
    }
}
