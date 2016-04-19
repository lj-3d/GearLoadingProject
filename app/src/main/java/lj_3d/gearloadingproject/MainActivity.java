package lj_3d.gearloadingproject;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import lj_3d.gearloadinglayout.enums.ShowMode;
import lj_3d.gearloadinglayout.enums.Type;
import lj_3d.gearloadinglayout.gearViews.GearDialogBuilder;
import lj_3d.gearloadinglayout.gearViews.OneGearLayout;
import lj_3d.gearloadinglayout.gearViews.ThreeGearsLayout;
import lj_3d.gearloadinglayout.gearViews.TwoGearsLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Type mSelectedType = Type.ONE_GEAR;
    private Resources mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResources = getResources();

        final View topButton = findViewById(R.id.btn_top);
        final View centerButton = findViewById(R.id.btn_center);
        final View bottomButton = findViewById(R.id.btn_bottom);
        final View leftButton = findViewById(R.id.btn_left);
        final View rightButton = findViewById(R.id.btn_rigth);

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_types);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.cb_one_gear:
                        mSelectedType = Type.ONE_GEAR;
                        break;
                    case R.id.cb_two_gears:
                        mSelectedType = Type.TWO_GEARS;
                        break;
                    case R.id.cb_three_gears:
                        mSelectedType = Type.THREE_GEARS;
                        break;
                }
            }
        });

        topButton.setTag(ShowMode.TOP);
        centerButton.setTag(ShowMode.CENTER);
        bottomButton.setTag(ShowMode.BOTTOM);
        leftButton.setTag(ShowMode.LEFT);
        rightButton.setTag(ShowMode.RIGHT);

        topButton.setOnClickListener(this);
        centerButton.setOnClickListener(this);
        bottomButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final ShowMode showMode = (ShowMode) v.getTag();
        switch (mSelectedType) {
            case ONE_GEAR:
                GearDialogBuilder.getInstance(MainActivity.this)
                        .setType(OneGearLayout.class)
                        .setShowDialogDuration(200)
                        .setShowMode(showMode)
                        .setDuration(3000)
                        .enableCutLayout(false)
                        .setDialogBackgroundAlpha(0.5f)
                        .setDialogBackgroundColor(mResources.getColor(R.color.colorAccent))
                        .setFirstGearColor(mResources.getColor(R.color.colorPrimaryDark))
                        .show();
                break;
            case TWO_GEARS:
                GearDialogBuilder.getInstance(MainActivity.this)
                        .setType(TwoGearsLayout.class)
                        .setShowDialogDuration(200)
                        .setShowMode(showMode)
                        .setDuration(3000)
                        .enableCutLayout(false)
                        .setDialogBackgroundAlpha(0.5f)
                        .setDialogBackgroundColor(mResources.getColor(R.color.colorAccent))
                        .setFirstGearColor(mResources.getColor(R.color.colorPrimaryDark))
                        .setSecondGearColor(mResources.getColor(R.color.dialog_stroke_color))
                        .show();
                break;
            case THREE_GEARS:
                GearDialogBuilder.getInstance(MainActivity.this)
                        .setType(ThreeGearsLayout.class)
                        .setShowDialogDuration(200)
                        .setShowMode(showMode)
                        .setDuration(3000)
                        .enableCutLayout(false)
                        .setDialogBackgroundAlpha(0.5f)
                        .setDialogBackgroundColor(mResources.getColor(R.color.colorAccent))
                        .setFirstGearColor(mResources.getColor(R.color.colorPrimaryDark))
                        .setSecondGearColor(mResources.getColor(R.color.dialog_stroke_color))
                        .setThirdGearColor(mResources.getColor(R.color.colorPrimary))
                        .show();
                break;
        }

    }
}
