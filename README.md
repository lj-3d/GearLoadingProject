# GearLoadingLayout

### Description 

 **GearLoadingLayout** widget for android that can be used as a progress bar, snack bar or yourself to create your own arrangements for their needs.
 
### Short Description

   - [**Use GearView as standalone widget**](#gear_view)
   
   - [**Use one of the three prepared layouts (One Gear, Two Gears, Three Gears)**](#prepared_layout)
    
   - [**Use GearDialogBuilder to create you loading dialog, with lot functionality**](#gear_dialog_builder)
 
 
   <a name="gear_view"></a>
### GearView
____

#### Samples

#### In Xml
                                               
    <lj_3d.gearloadinglayout.gearViews.GearView 
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        gear:mainDiameter="150dp"
        gear:secondDiameter="120dp"
        gear:innerDiameter="30dp"
        gear:enableCutCenter="true"
        gear:teethWidth="20dp"
        gear:mainColor="#3F51B5"
        gear:innerColor="#FF4081"
        gear:rotateAngle="10"/>                                        
   
#### In Code
   
     GearView gearView = new GearView(mContext);
           gearView.setMainDiameter(300);
           gearView.setSecondDiameter(240);
           gearView.setInnerDiameter(80);
           gearView.setTeethWidth(40);
           gearView.setRotateOffset(20);
           gearView.setColor(Color.BLUE);
           gearView.setInnerColor(Color.RED);
           gearView.enableCuttedCenter(true); // boolean param == true (center of GearView will be cutted)
           gearView.startSpinning(false); // start spinning animation, boolean param == true (rotate to the left side) | param == false (rotate to the right side)
   
     (and don`t forget add to your root view)
  
#### On Android Device
____

     
   Cutted Center                       |        Filled Center          
    :-------------------------:|:-------------------------:
    ![](https://lh4.googleusercontent.com/-gFgsbGQ3430/Vxh70Q6x3xI/AAAAAAAAC8k/nTbkZFXoQRcRiVm3YARNeHYVBNGW6gmnwCL0B/w259-h518-no/ezgif.com-video-to-gif.gif)|![](https://lh4.googleusercontent.com/-WrmYP7e7AQc/Vxh70T5RXLI/AAAAAAAAC8E/F_xocHLqEYQd7diXO_TjPgKkCSAgmTddwCL0B/w259-h518-no/ezgif.com-video-to-gif%25281%2529.gif)
     

   <a name="prepared_layout"></a>
### GearLayout

#### Features

 - Already preset layout
 - Small feature as CutOut Layout
 - Flexible functionality
 
#### Samples

#### In Xml

 - Three Gears
                 
                   <lj_3d.gearloadinglayout.gearViews.ThreeGearsLayout
                           android:layout_width="match_parent"
                           android:layout_height="match_parent"
                           gear:layoutAlpha="0.5"
                           gear:gearLayoutCutAlpha="0.5"
                           gear:cutLayoutVisibility="true"
                           gear:firstGearColor="@color/colorAccent"
                           gear:secondGearColor="@color/colorAccent"
                           gear:thirdGearColor="@color/colorAccent"
                           gear:firstGearCuttedCenter="false"
                           gear:secondGearCuttedCenter="false"
                           gear:thirdGearCuttedCenter="false"
                           gear:gearLayoutCutColor="@color/colorPrimaryDark"
                           gear:gearLayoutCutRadius="40dp"
                           gear:firstInnerGearColor="@color/colorPrimary"
                           gear:secondInnerGearColor="@color/colorPrimary"
                           gear:thirdInnerGearColor="@color/colorPrimary"/>  
                                               
 - Two Gears 
 
                    <lj_3d.gearloadinglayout.gearViews.TwoGearsLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            gear:layoutAlpha="0.5"
                            gear:gearLayoutCutAlpha="0.5"
                            gear:cutLayoutVisibility="true"
                            gear:firstGearColor="@color/colorAccent"
                            gear:secondGearColor="@color/colorAccent"
                            gear:firstGearCuttedCenter="false"
                            gear:secondGearCuttedCenter="false"
                            gear:gearLayoutCutColor="@color/colorPrimaryDark"
                            gear:gearLayoutCutRadius="40dp"
                            gear:firstInnerGearColor="@color/colorPrimary"
                            gear:secondInnerGearColor="@color/colorPrimary"/>
                            
 - One Gear
 
                    <lj_3d.gearloadinglayout.gearViews.OneGearLayout
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            gear:layoutAlpha="0.5"
                            gear:gearLayoutCutAlpha="0.5"
                            gear:cutLayoutVisibility="true"
                            gear:firstGearColor="@color/colorAccent"
                            gear:firstGearCuttedCenter="false"
                            gear:gearLayoutCutColor="@color/colorPrimaryDark"
                            gear:gearLayoutCutRadius="40dp"
                            gear:firstInnerGearColor="@color/colorPrimary"/>                            
   
#### In Code
   
 - One Gear
 
            OneGearLayout threeGearsLayout = new OneGearLayout(this);
                threeGearsLayout.setFirstGearColor(Color.WHITE);
                threeGearsLayout.setDialogBackgroundColor(Color.GREEN);
                threeGearsLayout.setDialogBackgroundAlpha(0.3f);
                threeGearsLayout.blurBackground(true);
                threeGearsLayout.enableCutLayout(false);
                threeGearsLayout.setCutRadius(80);
                threeGearsLayout.start();
   
 - Two Gears
    
           TwoGearsLayout threeGearsLayout = new TwoGearsLayout(this);
                   threeGearsLayout.setFirstGearColor(Color.WHITE);
                   threeGearsLayout.setSecondGearColor(Color.RED);
                   threeGearsLayout.setDialogBackgroundColor(Color.GREEN);
                   threeGearsLayout.setDialogBackgroundAlpha(0.3f);
                   threeGearsLayout.blurBackground(true);
                   threeGearsLayout.enableCutLayout(false);
                   threeGearsLayout.setCutRadius(80);
                   threeGearsLayout.start();

 - Three Gears
       
           ThreeGearsLayout threeGearsLayout = new ThreeGearsLayout(this);
                   threeGearsLayout.setFirstGearColor(Color.WHITE);
                   threeGearsLayout.setSecondGearColor(Color.RED);
                   threeGearsLayout.setThirdGearColor(Color.CYAN);
                   threeGearsLayout.setDialogBackgroundColor(Color.GREEN);
                   threeGearsLayout.setDialogBackgroundAlpha(0.3f);
                   threeGearsLayout.blurBackground(true);
                   threeGearsLayout.enableCutLayout(false);
                   threeGearsLayout.setCutRadius(80);
                   threeGearsLayout.start();


#### On Android Device

With CutLayout                       |        One Gear         |        Two Gears      |        Three Gears      
    :-------------------------:|:-------------------------::-------------------------:|:-------------------------:
    ![](https://lh6.googleusercontent.com/-Lx8R57vmH8U/VxjqpOCUupI/AAAAAAAAC9E/fitOcLShGHMFeWnQ0iTMpm1dcREJzJfNACL0B/w259-h518-no/ezgif.com-video-to-gif%25285%2529.gif)|![](https://lh4.googleusercontent.com/-Kf_1IowqPoE/VxjqoivPVPI/AAAAAAAAC9A/nfq2zRwzdtYfS0TIHnGoL8GgG4b727eqQCL0B/w259-h518-no/ezgif.com-video-to-gif%25282%2529.gif)|![](https://lh6.googleusercontent.com/-5ZFp1Y6atRM/Vxjqo4qRGTI/AAAAAAAAC88/Hpv6Vj0eWR0C9bu9KVriYO5X2T8yQF_MACL0B/w259-h518-no/ezgif.com-video-to-gif%25283%2529.gif)|![](https://lh4.googleusercontent.com/-o3AKESFpQZE/Vxjqo5hRm5I/AAAAAAAAC84/53m7dVv6o6EfWDxyrsA0GZRCkck9_xEowCL0B/w259-h518-no/ezgif.com-video-to-gif%25284%2529.gif)
     



   <a name="gear_dialog_builder"></a>
### GearDialogBuilder

#### Features

 - Fast create loading dialog
 - Enable Blur Effect (Thanks [This Stack Overflow Resource](http://stackoverflow.com/questions/2067955/fast-bitmap-blur-for-android-sdk))
 - Support modes : Dialog, Snackbar

#### Sample  

    GearDialogBuilder.getInstance(MainActivity.this)
                        .setType(ThreeGearsLayout.class)
                        .setShowDialogDuration(200)
                        .setShowMode(showMode)
                        .setDuration(3000)
                        .blurBackground(blur)
                        .enableCutLayout(false)
                        .setDialogBackgroundAlpha(0.5f)
                        .setDialogBackgroundColor(mResources.getColor(R.color.colorAccent))
                        .setFirstGearColor(mResources.getColor(R.color.colorPrimaryDark))
                        .setSecondGearColor(mResources.getColor(R.color.dialog_stroke_color))
                        .setThirdGearColor(mResources.getColor(R.color.colorPrimary))
                        .show();

#### On Android Device
               
 ![](https://lh4.googleusercontent.com/-PAkelJh1Q4o/Vxj0toiHoII/AAAAAAAAC9w/s67gZheCMBUU9CLd0zuu75_N3SX5ec98wCL0B/w259-h518-no/ezgif.com-video-to-gif%25287%2529.gif)
 
 
Android SDK Version
=========
 Min SDK Version == 1.
 
License
======

 Apache 2.0. See LICENSE file for details.
 
Author
=======
 
 Liubomyr Miller (lj-3d)