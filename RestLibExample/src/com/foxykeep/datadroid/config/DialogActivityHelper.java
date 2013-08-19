package com.foxykeep.datadroid.config;

import com.foxykeep.datadroidpoc.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class DialogActivityHelper {

    public static final int DIALOG_ERROR = 1;
    public static final int DIALOG_CONNEXION_ERROR = 2;
    public static final int DIALOG_PROGRESS = 3;
    
    private String mErrorDialogTitle;
    private String mErrorDialogMessage;
    private static final String SAVED_STATE_ERROR_TITLE = "savedStateErrorTitle";
    private static final String SAVED_STATE_ERROR_MESSAGE = "savedStateErrorMessage";
    
    public interface OnRetryClickListener {
    	public void onClick();
    }
    
	public DialogActivityHelper(final Context context, final Bundle savedInstanceState) {
		mErrorDialogTitle = context.getString(R.string.dialog_error_data_error_title);
		mErrorDialogMessage = context.getString(R.string.dialog_error_data_error_message);
		if (savedInstanceState != null) {
			mErrorDialogTitle = savedInstanceState
					.getString(SAVED_STATE_ERROR_TITLE);
			mErrorDialogMessage = savedInstanceState
					.getString(SAVED_STATE_ERROR_MESSAGE);
		}
	}
    
    public void onSaveInstanceState(final Bundle outState) {
        outState.putString(SAVED_STATE_ERROR_TITLE, mErrorDialogTitle);
        outState.putString(SAVED_STATE_ERROR_MESSAGE, mErrorDialogMessage);
    }
    
    public Dialog onCreateDialog(final Context context, final int id, final OnRetryClickListener listener) {
    	Builder b;
        switch (id) {
        case DIALOG_ERROR:
            b = new Builder(context);
            b.setTitle(mErrorDialogTitle);
            b.setMessage(mErrorDialogMessage);
            b.setCancelable(true);
            b.setNeutralButton(android.R.string.ok, null);
            return b.create();
        case DIALOG_CONNEXION_ERROR:
            b = new Builder(context);
            b.setCancelable(true);
            b.setNeutralButton(context.getString(android.R.string.ok), null);
            b.setPositiveButton(context.getString(R.string.dialog_button_retry),
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog,
                                final int which) {
                        	listener.onClick();
                        }
                    });
            b.setTitle(R.string.dialog_error_connexion_error_title);
            b.setMessage(R.string.dialog_error_connexion_error_message);
            return b.create();
        case DIALOG_PROGRESS:
            ProgressDialog dialog = new ProgressDialog(context);
            dialog.setTitle(R.string.progress_dialog_title);
            dialog.setMessage(context.getString(R.string.progress_dialog_message));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            return dialog;
        default:
            return null;
        }
    }
    
    public void onPrepareDialog(final int id, final Dialog dialog) {
        switch (id) {
        case DIALOG_ERROR:
            dialog.setTitle(mErrorDialogTitle);
            ((AlertDialog) dialog).setMessage(mErrorDialogMessage);
            break;
        default:
            break;
        }
    }
}
