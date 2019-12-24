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
import android.util.Base64;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("CharsetObjectCanBeUsed")
@RunWith(RobolectricTestRunner.class)
public class ActivationStringImporterTest {

    final static Context stringContext = RuntimeEnvironment.application;

    @Test
    public void CorrectString_TranslatesUrl() {
        assertEquals(
            "https://api-f9d7efcb.duosecurity.com/push/v2/activation/jvKkW95rJ85JPzHToAlb",
            new ActivationStringImporter(stringContext, "jvKkW95rJ85JPzHToAlb-YXBpLWY5ZDdlZmNiLmR1b3NlY3VyaXR5LmNvbQ").getActivationUrl().toString()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void BadString_Fails() {
        new ActivationStringImporter(stringContext, "!#@#").getActivationUrl();
    }

    @Test(expected = IllegalArgumentException.class)
    public void BadBase64_Fails() {
        new ActivationStringImporter(stringContext, "a=-b=").getActivationUrl();
    }

    /*
     * These tests require running an HTTP server at localhost:8000 serving out of the "docroot" directory.
     */

    @Test(expected = JSONException.class)
    public void MalformedHTTPResponse_Fails() throws IOException, JSONException {
        new ActivationStringImporter(stringContext, "malformed.json-"+Base64.encodeToString("localhost:8000".getBytes("UTF-8"), 0), "http", "GET").run();
    }

    @Test
    public void ServiceFailureHTTPResponse_FailsNoException() throws IOException, JSONException {
        assertNull(new ActivationStringImporter(stringContext, "duofailure.json-" + Base64.encodeToString("localhost:8000".getBytes("UTF-8"), 0), "http", "GET").run());

    }

    @Test
    public void CorrectHTTPResponse_Succeeds() throws IOException, JSONException {
        Intent intent = new ActivationStringImporter(stringContext, "correct.json-"+Base64.encodeToString("localhost:8000".getBytes("UTF-8"), 0), "http", "GET").run();
        assertEquals(
            "otpauth://hotp/"+stringContext.getString(R.string.service_name)+"?secret=NZSXILTTMNUW45DJNRWA&issuer=Test%20Customer&counter=0",
            Objects.requireNonNull(intent.getData()).toString()
        );
    }
}
