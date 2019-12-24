/*
 * Copyright 2019 Joey Hewitt <joey@joeyhewitt.com>
 *
 * This file is part of DuOTP.
 *
 * DuOTP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DuOTP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DuOTP.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package net.scintill.duotp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import androidx.annotation.Nullable;

public class ActivationStringImporterActivity extends Activity {
    private final String TAG = "Act.StringImporterAct.";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isHotpAppAvailable()) {
            // TODO i18n
            toast("There is no HOTP app available to receive the "+getString(R.string.service_name)+" HOTP secret. Canceling.");
        } else {
            String activationString = getIntent().getData().getHost();
            runActivationStringImporter(activationString);
        }
        finish();
    }

    // TODO asynctasks are apparently evil
    class ActivationStringImporterTask extends AsyncTask<String, Void, Object> {
        @Override
        protected Object doInBackground(String... params) {
            String activationString = params[0];
            try {
                Intent intent = new ActivationStringImporter(ActivationStringImporterActivity.this, activationString).run();
                if (intent == null) {
                    // TODO i18n
                    return new RuntimeException(getString(R.string.service_name)+" returned a failure code.");
                }
                ActivationStringImporterActivity.this.startActivity(intent);
            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (result instanceof Exception) {
                Exception e = (Exception) result;
                Log.e(TAG, "Error", e);
                // TODO i18n and more detail
                toast("There was an error");
            }
        }
    }

    private void runActivationStringImporter(String activationString) {
        new ActivationStringImporterTask().execute(activationString);
    }

    private void toast(String text) {
        text = getString(R.string.app_name) + ": " + text;
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private boolean isHotpAppAvailable() {
        Intent intent;
        try {
            intent = ActivationStringImporter.makeHotpIntent(getString(R.string.service_name), "", "");
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        return intent.resolveActivity(getPackageManager()) != null;
    }
}
