package com.foxykeep.datadroidpoc.ui;

import ru.igarin.base.restlib.service.RequestManager;
import ru.igarin.base.restlib.service.RequestManager.OnRequestFinishedListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.foxykeep.datadroid.config.DialogConfig;
import com.foxykeep.datadroid.config.Lg;
import com.foxykeep.datadroid.config.PH;
import com.foxykeep.datadroid.config.ThisService;
import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroid.data.PhoneRequestParams;
import com.foxykeep.datadroid.workers.WorkerAddPhoneList;
import com.foxykeep.datadroid.workers.WorkerDeletePhoneList;
import com.foxykeep.datadroid.workers.WorkerEditPhoneList;
import com.foxykeep.datadroid.workers.WorkerSyncPhoneList;
import com.foxykeep.datadroidpoc.R;
import com.foxykeep.datadroidpoc.util.UserManager;

public class MainActvity extends BaseListActivity implements
        OnRequestFinishedListener {

    private static final String SAVED_STATE_REQUEST_ID = "savedStateRequestId";
    private static final String SAVED_STATE_REQUEST_TYPE = "savedStateRequestType";
    private static final String SAVED_STATE_ERROR_TITLE = "savedStateErrorTitle";
    private static final String SAVED_STATE_ERROR_MESSAGE = "savedStateErrorMessage";
    private static final String SAVED_STATE_POSITION_TO_DELETE = "savedStatePositionToDelete";
    private static final String SAVED_STATE_ARE_PHONES_LOADED = "savedStateIsResultLoaded";

    private static final int REQUEST_TYPE_LIST = 1;
    private static final int REQUEST_TYPE_DELETE_MONO = 2;
    private static final int REQUEST_TYPE_DELETE_ALL = 3;
    private static final int REQUEST_TYPE_ADD = 4;
    private static final int REQUEST_TYPE_EDIT = 5;

    public static final String RESULT_EXTRA_ADDED_PHONE = "resultExtraAddedPhone";
    public static final String RESULT_EXTRA_EDITED_PHONE = "resultExtraEditedPhone";
    public static final String RESULT_EXTRA_DELETED_PHONE_ID = "resultExtraDeletedPhoneId";

    private RequestManager mRequestManager;
    private int mRequestId = -1;
    private int mRequestType = -1;
    private String mUserId;

    private boolean mArePhonesLoaded = false;

    private String mErrorDialogTitle;
    private String mErrorDialogMessage;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.crud_phone_list);

        if (savedInstanceState != null) {
            mRequestId = savedInstanceState.getInt(SAVED_STATE_REQUEST_ID, -1);
            mRequestType = savedInstanceState.getInt(SAVED_STATE_REQUEST_TYPE,
                    -1);
            mErrorDialogTitle = savedInstanceState
                    .getString(SAVED_STATE_ERROR_TITLE);
            mErrorDialogMessage = savedInstanceState
                    .getString(SAVED_STATE_ERROR_MESSAGE);
            mPositionToDelete = savedInstanceState
                    .getInt(SAVED_STATE_POSITION_TO_DELETE);
            mArePhonesLoaded = savedInstanceState.getBoolean(
                    SAVED_STATE_ARE_PHONES_LOADED, false);

        }

        mRequestManager = RequestManager.from(ThisService.class, this);
        mUserId = UserManager.getUserId(this);

        String[] bindFrom = new String[] { "name",
                "manufacturer" };
        int[] bindTo = new int[] { R.id.tv_name, R.id.tv_manufacturer };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.crud_phone_list_item,  PH.from(this).query(Phone.class, null, null, null, null),
                bindFrom, bindTo);
        setListAdapter(adapter);
        final ListView listView = getListView();
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);

        callSyncPhoneListWS();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestId != -1) {
            if (mRequestManager.isRequestInProgress(mRequestId)) {
                mRequestManager.addOnRequestFinishedListener(this);
                setProgressBarIndeterminateVisibility(true);
            } else {
                mArePhonesLoaded = true;
                updateAdapter();
                mRequestId = -1;
            }
        } else if (!mArePhonesLoaded) {
            callSyncPhoneListWS();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRequestId != -1) {
            mRequestManager.removeOnRequestFinishedListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SAVED_STATE_REQUEST_ID, mRequestId);
        outState.putInt(SAVED_STATE_REQUEST_TYPE, mRequestType);
        outState.putString(SAVED_STATE_ERROR_TITLE, mErrorDialogTitle);
        outState.putString(SAVED_STATE_ERROR_MESSAGE, mErrorDialogMessage);
        outState.putInt(SAVED_STATE_POSITION_TO_DELETE, mPositionToDelete);
        outState.putBoolean(SAVED_STATE_ARE_PHONES_LOADED, mArePhonesLoaded);
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        Builder b;
        switch (id) {
        case DialogConfig.DIALOG_ERROR:
            b = new Builder(this);
            b.setTitle(mErrorDialogTitle);
            b.setMessage(mErrorDialogMessage);
            b.setCancelable(true);
            b.setNeutralButton(android.R.string.ok, null);
            return b.create();
        case DialogConfig.DIALOG_CONNEXION_ERROR:
            b = new Builder(this);
            b.setCancelable(true);
            b.setNeutralButton(getString(android.R.string.ok), null);
            b.setPositiveButton(getString(R.string.dialog_button_retry),
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog,
                                final int which) {
                            if (mRequestType == REQUEST_TYPE_LIST) {
                                callSyncPhoneListWS();
                            } else if (mRequestType == REQUEST_TYPE_DELETE_ALL) {
                                callSyncPhoneDeleteAllWS();
                            } else if (mRequestType == REQUEST_TYPE_DELETE_MONO) {
                                callSyncPhoneDeleteMonoWS();
                            } else if (mRequestType == REQUEST_TYPE_ADD) {
                                callSyncPhoneAddWS(mPhoneToAddUpdate);
                            } else if (mRequestType == REQUEST_TYPE_EDIT) {
                                callSyncPhoneEditWS(mPhoneToAddUpdate);
                            }
                        }
                    });
            b.setTitle(R.string.dialog_error_connexion_error_title);
            b.setMessage(R.string.dialog_error_connexion_error_message);
            return b.create();
        case DialogConfig.DIALOG_PROGRESS:
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle(R.string.progress_dialog_title);
            dialog.setMessage(getString(R.string.progress_dialog_message));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            return dialog;
        default:
            return super.onCreateDialog(id);
        }
    }

    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {
        switch (id) {
        case DialogConfig.DIALOG_ERROR:
            dialog.setTitle(mErrorDialogTitle);
            ((AlertDialog) dialog).setMessage(mErrorDialogMessage);
            break;
        default:
            super.onPrepareDialog(id, dialog);
            break;
        }
    }

    private void callSyncPhoneListWS() {
        setProgressBarIndeterminateVisibility(true);
        mRequestManager.addOnRequestFinishedListener(this);
        mRequestType = REQUEST_TYPE_LIST;

        PhoneRequestParams param = new PhoneRequestParams();
        param.userId = mUserId;
        param.setPhone(new Phone());
        
        mRequestId = mRequestManager.startRequestForWorker(WorkerSyncPhoneList.class,
                param);
    }

    protected void callSyncPhoneDeleteMonoWS() {
        mRequestType = REQUEST_TYPE_DELETE_MONO;
        Cursor c = (Cursor) ((SimpleCursorAdapter) getListAdapter())
                .getItem(mPositionToDelete);
        Phone phone = (Phone) PH.from(this).getFromCursor(Phone.class, c);
        callSyncPhoneDeleteWS(String.valueOf(phone.id));
    }

    protected void callSyncPhoneDeleteAllWS() {
        final StringBuilder sb = new StringBuilder();
        final SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        Cursor c = adapter.getCursor();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Phone phone = (Phone) PH.from(this).getFromCursor(Phone.class, c);

            sb.append(phone.id);
            if (c.moveToNext()) {
                sb.append(",");
            }
        }
        mRequestType = REQUEST_TYPE_DELETE_ALL;
        callSyncPhoneDeleteWS(sb.toString());
    }

    private void callSyncPhoneDeleteWS(final String phoneIdList) {
        showDialog(DialogConfig.DIALOG_PROGRESS);
        mRequestManager.addOnRequestFinishedListener(this);

        PhoneRequestParams param = new PhoneRequestParams();
        param.userId = mUserId;
        param.setPhone(new Phone());
        param.phoneIds = phoneIdList;
        
        mRequestId = mRequestManager.startRequestForWorker(WorkerDeletePhoneList.class,
                param);
    }

    protected void callSyncPhoneAddWS(Phone phone) {
        showDialog(DialogConfig.DIALOG_PROGRESS);
        mRequestManager.addOnRequestFinishedListener(this);
        mRequestType = REQUEST_TYPE_ADD;

        PhoneRequestParams param = new PhoneRequestParams();
        param.userId = mUserId;
        param.setPhone(phone);
        
        mRequestId = mRequestManager.startRequestForWorker(WorkerAddPhoneList.class,
                param);
    }

    protected void callSyncPhoneEditWS(Phone phone) {
        showDialog(DialogConfig.DIALOG_PROGRESS);
        mRequestManager.addOnRequestFinishedListener(this);
        mRequestType = REQUEST_TYPE_EDIT;

        PhoneRequestParams param = new PhoneRequestParams();
        param.userId = mUserId;
        param.setPhone(phone);

        mRequestId = mRequestManager.startRequestForWorker(WorkerEditPhoneList.class,
                param);
    }

    private void updateAdapter() {
        Cursor c =  PH.from(this).query(Phone.class, null, null, null, null);

        final SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        adapter.changeCursor(c);
        adapter.notifyDataSetChanged();
    }

	@Override
	public Phone getPhoneOnPosition(int position) {
        Cursor c = (Cursor) ((SimpleCursorAdapter) getListAdapter())
                .getItem(position);
        return (Phone) PH.from(this).getFromCursor(Phone.class, c);
	}

    @Override
    public void onRequestFinished(int requestId, Bundle resultData) {
        Lg.d("onRequestFinished");
        if (requestId == mRequestId) {
            if (mRequestType == REQUEST_TYPE_LIST) {
                setProgressBarIndeterminateVisibility(false);
            } else if (mRequestType == REQUEST_TYPE_DELETE_ALL
                    || mRequestType == REQUEST_TYPE_DELETE_MONO
                    || mRequestType == REQUEST_TYPE_ADD
                    || mRequestType == REQUEST_TYPE_EDIT) {
                dismissDialog(DialogConfig.DIALOG_PROGRESS);
            }
            mRequestId = -1;
            mRequestManager.removeOnRequestFinishedListener(this);

            if (mRequestType == REQUEST_TYPE_LIST) {
                mArePhonesLoaded = true;
            }
            updateAdapter();
            mRequestType = -1;
        }
    }

    @Override
    public void onRequestConnectionError(int requestId, int statusCode) {
        Lg.d("onConnexionError");
        if (requestId == mRequestId) {
            if (mRequestType == REQUEST_TYPE_LIST) {
                setProgressBarIndeterminateVisibility(false);
            } else if (mRequestType == REQUEST_TYPE_DELETE_ALL
                    || mRequestType == REQUEST_TYPE_DELETE_MONO
                    || mRequestType == REQUEST_TYPE_ADD
                    || mRequestType == REQUEST_TYPE_EDIT) {
                dismissDialog(DialogConfig.DIALOG_PROGRESS);
            }
            mRequestId = -1;
            mRequestManager.removeOnRequestFinishedListener(this);
            showDialog(DialogConfig.DIALOG_CONNEXION_ERROR);
        }
    }

    @Override
    public void onRequestDataError(int requestId) {
        Lg.d("onHandleError");
        if (requestId == mRequestId) {
            if (mRequestType == REQUEST_TYPE_LIST) {
                setProgressBarIndeterminateVisibility(false);
            } else if (mRequestType == REQUEST_TYPE_DELETE_ALL
                    || mRequestType == REQUEST_TYPE_DELETE_MONO
                    || mRequestType == REQUEST_TYPE_ADD
                    || mRequestType == REQUEST_TYPE_EDIT) {
                dismissDialog(DialogConfig.DIALOG_PROGRESS);
            }
            mRequestId = -1;
            mRequestManager.removeOnRequestFinishedListener(this);
            mErrorDialogTitle = getString(R.string.dialog_error_data_error_title);
            mErrorDialogMessage = getString(R.string.dialog_error_data_error_message);
            showDialog(DialogConfig.DIALOG_ERROR);
        }
    }

    @Override
    public void onRequestCustomError(int requestId, Bundle resultData) {

    }
}
