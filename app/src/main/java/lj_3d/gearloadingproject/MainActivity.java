package lj_3d.gearloadingproject;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import lj_3d.gearloadinglayout.enums.ShowMode;
import lj_3d.gearloadinglayout.enums.Style;
import lj_3d.gearloadinglayout.enums.Type;
import lj_3d.gearloadinglayout.gearViews.GearView;
import lj_3d.gearloadinglayout.utils.GearDialogBuilder;
import lj_3d.gearloadinglayout.gearViews.OneGearLayout;
import lj_3d.gearloadinglayout.gearViews.ThreeGearsLayout;
import lj_3d.gearloadinglayout.gearViews.TwoGearsLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Type mSelectedType = Type.ONE_GEAR;
    private Resources mResources;
    private boolean blur;
    private Style mStyle = Style.SNACK_BAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup rootView = (ViewGroup) View.inflate(this, R.layout.activity_main, null);
        setContentView(rootView);

        mResources = getResources();

        final View topButton = findViewById(R.id.btn_top);
        final View centerButton = findViewById(R.id.btn_center);
        final View bottomButton = findViewById(R.id.btn_bottom);
        final View leftButton = findViewById(R.id.btn_left);
        final View rightButton = findViewById(R.id.btn_rigth);

        final CheckBox enableBlur = (CheckBox) findViewById(R.id.cb_blur);
        final CheckBox style = (CheckBox) findViewById(R.id.cb_mode);

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

        enableBlur.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                blur = isChecked;
            }
        });

        style.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mStyle = isChecked ? Style.SNACK_BAR : Style.DIALOG;
            }
        });

        enableBlur.setChecked(true);
        style.setChecked(true);
//
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
                        .blurBackground(blur)
                        .enableCutLayout(true)
                        .setCutLayoutAlpha(0.5f)
                        .setStyle(mStyle)
                        .setDialogBackgroundColor(Color.TRANSPARENT)
                        .setMainBackgroundColor(Color.TRANSPARENT)
                        .setCutLayoutColor(mResources.getColor(android.R.color.white))
                        .setFirstGearColor(mResources.getColor(android.R.color.white))
                        .show();
                break;
            case TWO_GEARS:
                GearDialogBuilder.getInstance(MainActivity.this)
                        .setType(TwoGearsLayout.class)
                        .setShowDialogDuration(200)
                        .setShowMode(showMode)
                        .setDuration(3000)
                        .blurBackground(blur)
                        .enableCutLayout(true)
                        .setCutLayoutAlpha(0.5f)
                        .setStyle(mStyle)
                        .setDialogBackgroundColor(Color.TRANSPARENT)
                        .setMainBackgroundColor(Color.TRANSPARENT)
                        .setCutLayoutColor(mResources.getColor(android.R.color.white))
                        .setFirstGearColor(mResources.getColor(android.R.color.white))
                        .setSecondGearColor(mResources.getColor(android.R.color.white))
                        .show();
                break;
            case THREE_GEARS:
                GearDialogBuilder.getInstance(MainActivity.this)
                        .setType(ThreeGearsLayout.class)
                        .setShowDialogDuration(200)
                        .setShowMode(showMode)
                        .setDuration(3000)
                        .blurBackground(blur)
                        .enableCutLayout(true)
                        .setCutRadius(80)
                        .setCutLayoutAlpha(0.5f)
                        .setStyle(mStyle)
                        .setDialogBackgroundColor(Color.TRANSPARENT)
                        .setMainBackgroundColor(Color.TRANSPARENT)
                        .setShadowWidth(getResources().getDimensionPixelSize(R.dimen.shadow_width))
                        .setCutLayoutColor(mResources.getColor(android.R.color.white))
                        .setFirstGearColor(mResources.getColor(android.R.color.white))
                        .setSecondGearColor(mResources.getColor(android.R.color.white))
                        .setThirdGearColor(mResources.getColor(android.R.color.white))
                        .show();
                break;
        }

    }
}
