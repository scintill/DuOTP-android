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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import com.google.android.apps.authenticator.util.Base32String;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.Nullable;

// With thanks to https://github.com/simonseo/nyuad-spammer/blob/master/spammer/duo/duo.py
@SuppressWarnings("CharsetObjectCanBeUsed")
class ActivationStringImporter {

    @SuppressWarnings("unused")
    private static final String TAG = "ActivationStringImporter";

    private Context stringContext;
    private String activationString;
    private String urlScheme = "https", httpMethod = "POST";

    ActivationStringImporter(Context stringContext, String activationString) {
        this.stringContext = stringContext;
        this.activationString = activationString;
    }

    // for testing. https and POST should be used normally
    ActivationStringImporter(Context stringContext, String activationString, String urlScheme, String httpMethod) {
        this(stringContext, activationString);
        this.urlScheme = urlScheme;
        this.httpMethod = httpMethod;
    }

    /*package*/ URL getActivationUrl() {
        String[] parts = this.activationString.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("malformed QR string");
        }
        String activationToken = parts[0], hostnameB64 = parts[1];
        String hostname;
        try {
            hostname = new String(Base64.decode(hostnameB64, 0), "UTF-8");
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalArgumentException("malformed part of QR string", e);
        }

        try {
            return new URL(this.urlScheme + "://" + hostname + "/push/v2/activation/" + activationToken);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * @param body parsed JSON body object
     * @return Intent the intent to import into OTP app, or null if the service response indicated failure
     * @throws IllegalArgumentException data is not formatted correctly
     */
    private Intent getHotpIntentFromJson(JSONObject body) {
        String hotpSecret;
        String customerName;
        try {
            if (!body.get("stat").equals("OK")) {
                return null;
            }
            JSONObject response = (JSONObject) body.get("response");
            hotpSecret = (String) response.get("hotp_secret");
            customerName = (String) response.get("customer_name");
        } catch (JSONException e) {
            throw new IllegalArgumentException("JSON object not formatted correctly", e);
        }

        try {
            return makeHotpIntent(stringContext.getString(R.string.service_name), hotpSecret, customerName);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }


    // Not on main thread
    @Nullable
    Intent run() throws IOException, JSONException {
        URL activationUrl = getActivationUrl();
        HttpURLConnection urlConn = (HttpURLConnection) activationUrl.openConnection();

        JSONObject response;
        try {
            urlConn.setRequestMethod(this.httpMethod);
            if (!this.httpMethod.equals("GET")) {
                urlConn.setDoOutput(true);
                urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConn.getOutputStream().write(
                    "app_id=com.duosecurity.duomobile&app_version=3.37.1&app_build_number=337101"
                    .getBytes("UTF-8")
                );
                urlConn.getOutputStream().close();
            }
            urlConn.setConnectTimeout(5);
            urlConn.setReadTimeout(5);

            response = new JSONObject(new java.util.Scanner(urlConn.getInputStream()).useDelimiter("\\A").next());
        } finally {
            urlConn.disconnect();
        }

        return getHotpIntentFromJson(response);
    }

    /* package */static Intent makeHotpIntent(String name, String secret, String issuer) throws UnsupportedEncodingException {
        // https://github.com/google/google-authenticator/wiki/Key-Uri-Format
        String uri = "otpauth://hotp/" + Uri.encode(name) +
                "?secret=" + Base32String.encode(secret.getBytes("UTF-8")) +
                "&issuer=" + Uri.encode(issuer) +
                "&counter=0";
        return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    }

}
