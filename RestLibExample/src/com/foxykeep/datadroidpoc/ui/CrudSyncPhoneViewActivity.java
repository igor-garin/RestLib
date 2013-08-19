/*
 * 2011 Foxykeep (http://datadroid.foxykeep.com)
 *
 * Licensed under the Beerware License :
 * 
 *   As long as you retain this notice you can do whatever you want with this stuff. If we meet some day, and you think
 *   this stuff is worth it, you can buy me a beer in return
 */
package com.foxykeep.datadroidpoc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroidpoc.R;

public class CrudSyncPhoneViewActivity extends Activity {

    private static final String SAVED_STATE_PHONE = "savedStatePhone";

    public static final String INTENT_EXTRA_PHONE = "com.foxykeep.datadroidpoc.ui.extras.phone";

    private Phone mPhone;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crud_phone_view);

        Intent intent = getIntent();

        if (savedInstanceState != null) {
            mPhone = Phone.getPhone(savedInstanceState.getString(SAVED_STATE_PHONE));
        } else if (intent != null) {
            mPhone = Phone.getPhone(intent.getStringExtra(INTENT_EXTRA_PHONE));
        }

        populateViews();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_PHONE, Phone.getPhone(mPhone));
    }

    private void populateViews() {
        ((TextView) findViewById(R.id.tv_name)).setText(mPhone.name);
        ((TextView) findViewById(R.id.tv_manufacturer))
                .setText(mPhone.manufacturer);
        ((TextView) findViewById(R.id.tv_android_version))
                .setText(mPhone.androidVersion);
        ((TextView) findViewById(R.id.tv_screen_size)).setText(getString(
                R.string.crud_phone_view_tv_screen_size_format,
                mPhone.screenSize));
        ((TextView) findViewById(R.id.tv_price)).setText(getString(
                R.string.crud_phone_view_tv_price_format, mPhone.price));
    }

}
