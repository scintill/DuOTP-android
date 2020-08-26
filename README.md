# DuOTP

DuOTP is a small GPL3+ Android app that allows you to log in to Duo*-protected services with a standard [OTP](https://en.wikipedia.org/wiki/One-time_password "one-time password") app, such as [Google Authenticator](https://github.com/google/google-authenticator-android).

DuOTP has no visible interface or app drawer icon. An OTP-capable app must also be installed (Google Authenticator or similar.) See below for usage instructions.

## Usage

(Disclaimer: I don't know how much of this could vary between sites, so I can only describe how the one I tested with works.)

1. **Get your Android device with DuOTP installed and enter incognito mode**. (Incognito so that any previous login sessions are ignored.) Begin logging into the protected service on your Android device.
1. When you get to the Duo login screen, choose `Add a new device` and authenticate if needed.
1. Choose to activate a `Tablet`, then choose `Android`.
1. Click `I have Duo Mobile installed`, then `Take me to Duo Mobile`.
1. Your OTP app should then open, asking to confirm importing the secret token. If an error occurs, DuOTP will pop up an error message. If nothing happens, then DuOTP is not installed correctly, or your browser is not activating it.
1. After you have saved the account in your OTP app, return to the browser. Tap `Continue` and `Continue to Login`. Now and any future time you are prompted by the Duo login screen, choose `Enter a Passcode` and copy the numeric code from your OTP app.

After this, you don't need to use DuOTP again, and may remove it from your device if you'd like. (It's small though, so it might be worth keeping around in case Duo expires the secret token.)

## Tested with

* Google Authenticator for Android v5.00, GitHub release APK
* [`de.kuix.android.apps.authenticator2`](https://github.com/kaie/otp-authenticator-android) version 1.0
* [andOTP](https://github.com/andOTP/andOTP) version 0.7.1.1
* Firefox for Android 68.3.0 (for browsing through the import process)

## Some technical details
This app is activated by tapping a link with sceheme `duo://` from a Duo-enabled web page. DuOTP then loads the HOTP token from a Duo URL and passes it to your OTP app (any app supporting the `otpauth://` scheme.) Most of this logic is in the `ActivationStringImporter` class.

## Possible future enhancements and open questions

* Does the token expire after some time? (Mine's still working after 8 months.) The JSON response from the service does include a `reactivation_token` field that DuOTP currently ignores.
    * If it does expire on you, you can re-do the import process and I imagine you'll get a new secret and be fine until another expiration.
* QR code enrollment: if you begin logging in on a PC, you have the option to enroll via Duo Mobile with a QR code displayed on the PC.
    * I believe the code contains the same data as the `duo://` link you are tapping in my usage instructions above, but without a URI scheme prefix. If you would like to use the QR code and can DIY a bit, you should be able to decode the QR code into text, add `duo://` on the beginning, generate a new QR code from that result, and scan that in your phone's browser, and it will be imported by DuOTP and your OTP app.
* Supporting push notifications: I guess this app's target audience won't like the non-libre dependencies that probably come with that.

## License

```
Copyright 2019 Joey Hewitt <joey@joeyhewitt.com>

DuOTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

DuOTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
```

The full GPLv3 license text is included in the file `COPYING`.

A file under Apache License Version 2.0 is included in `app/src/main/java/com/google/android/apps/authenticator/util/Base32String.java`.

\* **Note**: this app is not associated with or endorsed by Duo Security, Inc. or Cisco Systems, Inc.

Thanks to https://github.com/simonseo/nyuad-spammer/blob/master/spammer/duo/duo.py for giving some hints about the enrollment process.
