::WIN BATCH SCRIPT

:: CHANGE THESE
set app_package=com.liner.linerlaucnher
set dir_app_name=OTAUpdates
set MAIN_ACTIVITY=MainActivity

set ADB="adb"
::ADB_SH="%ADB% shell" # this script assumes using `adb root`. for `adb su` 
see `Caveats`

set path_sysapp=/system/priv-app
set apk_host=C:\Users\LineR\Documents\AndroidStudioProjects\LinerOTA-new\app\build\outputs\apk\debug\app-debug.apk
set apk_name=%dir_app_name%.apk
set apk_target_dir=%path_sysapp%/%dir_app_name%
set apk_target_sys=%apk_target_dir%/%apk_name%



set ADB_SH_SU=%ADB% shell su -c
set ADB_SH=%ADB% shell
%ADB_SH% reboot recovery
pause
%ADB_SH% twrp mount system
%ADB_SH% twrp remountrw system
::%ADB_SH% mkdir -p /sdcard/tmp
%ADB% push %apk_host% %apk_target_dir%/%apk_name%
%ADB_SH% chmod 644 %apk_target_sys%
%ADB_SH% reboot
pause
