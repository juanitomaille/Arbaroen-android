# Arbaroen-android
Use rotary encoders to control audio in android car system

In a personal project of embedded android system based on a ODROID XU4, I needed 2 rotary encoders to control playback for more safety in driving.
This is the Android side.

An arduino nano runs the 2 rotary encoders, plugged by usb serial port.

in the repository named Arbaroen-Arduino, you have the arduino code.


For signing app, use platform keys due to USB_MANAGE privilege

For signing inside android-studio, use keytool-import script in signing APK directory
(https://devarea.com/signing-android-application/#.Xi73D-HjIUG)

After signing, zipalign app with

```
zipalign -fv 4 YourApplication-signed.apk YourApplication-aligned.apk
```
