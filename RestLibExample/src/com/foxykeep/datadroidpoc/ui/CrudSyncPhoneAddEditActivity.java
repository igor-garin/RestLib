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
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroidpoc.R;

public class CrudSyncPhoneAddEditActivity extends Activity implements OnClickListener,
        TextWatcher {

    public static final String INTENT_EXTRA_PHONE = "com.foxykeep.datadroidpoc.ui.extras.phone";

    private EditText mEditTextName;
    private EditText mEditTextManufacturer;
    private EditText mEditTextAndroidVersion;
    private EditText mEditTextScreenSize;
    private EditText mEditTextPrice;
    private Button mButtonSubmit;

    private Phone mPhone;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.crud_phone_add_edit);
        bindViews();

        final Intent intent = getIntent();
        if (intent != null) {
            mPhone = Phone.getPhone(intent.getStringExtra(INTENT_EXTRA_PHONE));
        }

        setTitle(mPhone == null ? R.string.crud_sync_phone_add_title : R.string.crud_sync_phone_edit_title);

        populateViews();
    }

    private void bindViews() {
        mEditTextName = (EditText) findViewById(R.id.et_name);
        mEditTextName.addTextChangedListener(this);
        mEditTextManufacturer = (EditText) findViewById(R.id.et_manufacturer);
        mEditTextManufacturer.addTextChangedListener(this);
        mEditTextAndroidVersion = (EditText) findViewById(R.id.et_android_version);
        mEditTextAndroidVersion.addTextChangedListener(this);
        mEditTextScreenSize = (EditText) findViewById(R.id.et_screen_size);
        mEditTextScreenSize.addTextChangedListener(this);
        mEditTextPrice = (EditText) findViewById(R.id.et_price);
        mEditTextPrice.addTextChangedListener(this);

        mButtonSubmit = (Button) findViewById(R.id.b_submit);
        mButtonSubmit.setOnClickListener(this);
    }

    private void populateViews() {
        if (mPhone != null) {
            mEditTextName.setText(mPhone.name);
            mEditTextManufacturer.setText(mPhone.manufacturer);
            mEditTextAndroidVersion.setText(mPhone.androidVersion);
            mEditTextScreenSize.setText(new SpannableString(String.valueOf(mPhone.screenSize)));
            mEditTextPrice.setText(new SpannableString(String.valueOf(mPhone.price)));
        }
    }

    @Override
    public void onClick(final View view) {
        if (view == mButtonSubmit) {
            if (mPhone == null) {
                mPhone = new Phone();
            }
            mPhone.name = mEditTextName.getText().toString();
            mPhone.manufacturer = mEditTextManufacturer.getText().toString();
            mPhone.androidVersion = mEditTextAndroidVersion.getText().toString();
            mPhone.screenSize = Double.parseDouble(mEditTextScreenSize.getText().toString());
            mPhone.price = Integer.parseInt(mEditTextPrice.getText().toString());
            
            Intent resultData = new Intent();
            resultData.putExtra(INTENT_EXTRA_PHONE, Phone.getPhone(mPhone));
            setResult(RESULT_OK, resultData);
            finish();
        }
    }

    @Override
    public void afterTextChanged(final Editable s) {
        mButtonSubmit.setEnabled(!TextUtils.isEmpty(mEditTextName.getText().toString())
                && !TextUtils.isEmpty(mEditTextManufacturer.getText().toString())
                && !TextUtils.isEmpty(mEditTextAndroidVersion.getText().toString())
                && !TextUtils.isEmpty(mEditTextScreenSize.getText().toString())
                && !TextUtils.isEmpty(mEditTextPrice.getText().toString()));
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
    }
}
