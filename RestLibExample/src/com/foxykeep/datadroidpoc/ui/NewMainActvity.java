package com.foxykeep.datadroidpoc.ui;

import ru.igarin.base.restlib.provider.PoCHelper;
import ru.igarin.base.restlib.service.SingleRequestManagerActivityHelper;
import ru.igarin.base.restlib.service.SingleRequestManagerActivityHelper.RequestListener;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.foxykeep.datadroid.config.ThisProvider;
import com.foxykeep.datadroid.config.ThisService;
import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroid.data.PhoneRequestParams;
import com.foxykeep.datadroid.workers.WorkerAddPhoneList;
import com.foxykeep.datadroid.workers.WorkerDeletePhoneList;
import com.foxykeep.datadroid.workers.WorkerEditPhoneList;
import com.foxykeep.datadroid.workers.WorkerSyncPhoneList;
import com.foxykeep.datadroidpoc.R;
import com.foxykeep.datadroidpoc.util.DialogActivityHelper;
import com.foxykeep.datadroidpoc.util.DialogConfig;
import com.foxykeep.datadroidpoc.util.UserManager;
import com.foxykeep.datadroidpoc.util.DialogActivityHelper.OnRetryClickListener;

public class NewMainActvity extends BaseListActivity implements RequestListener {
	
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

    private String mUserId;

    private boolean mArePhonesLoaded = false;
    
    private DialogActivityHelper mDialogHelper = null;
    private SingleRequestManagerActivityHelper<ThisService> mRequestHelper = null;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.crud_phone_list);
        
        mDialogHelper = new DialogActivityHelper(this, savedInstanceState);
        mRequestHelper = new SingleRequestManagerActivityHelper<ThisService>(ThisService.class, this, savedInstanceState, this);

        if (savedInstanceState != null) {
            mPositionToDelete = savedInstanceState
                    .getInt(SAVED_STATE_POSITION_TO_DELETE);
            mArePhonesLoaded = savedInstanceState.getBoolean(
                    SAVED_STATE_ARE_PHONES_LOADED, false);
        }

        mUserId = UserManager.getUserId(this);

        String[] bindFrom = new String[] { "name",
                "manufacturer" };
        int[] bindTo = new int[] { R.id.tv_name, R.id.tv_manufacturer };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.crud_phone_list_item,  PoCHelper.init(ThisProvider.class).setContext(this).setClass(Phone.class).executeQuery(),
                bindFrom, bindTo);
        setListAdapter(adapter);
        final ListView listView = getListView();
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mRequestHelper.onResume();
        
        if (!mArePhonesLoaded) {
            callSyncPhoneListWS();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mRequestHelper.onPause();
    }
    
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        mRequestHelper.onSaveInstanceState(outState);
        mDialogHelper.onSaveInstanceState(outState);
        
        outState.putInt(SAVED_STATE_POSITION_TO_DELETE, mPositionToDelete);
        outState.putBoolean(SAVED_STATE_ARE_PHONES_LOADED, mArePhonesLoaded);
    }
    
    @Override
    protected Dialog onCreateDialog(final int id) {
    	Dialog dialog = mDialogHelper.onCreateDialog(this, id, new OnRetryClickListener() {

			@Override
			public void onClick() {
				mRequestHelper.retryLastRequest();
			}}
    	);
    	if(dialog == null) {
    		return super.onCreateDialog(id);
    	} else {
    		return dialog;
    	}
    }
    
    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {
    	mDialogHelper.onPrepareDialog(id, dialog);
    	super.onPrepareDialog(id, dialog);
    }
    
    private void callSyncPhoneListWS() {

        PhoneRequestParams param = new PhoneRequestParams();
        param.userId = mUserId;
        
        mRequestHelper.startRequestForWorker(REQUEST_TYPE_LIST, WorkerSyncPhoneList.class, param);
    }

    @Override
	protected Phone getPhoneOnPosition(int position) {
        Cursor c = (Cursor) ((SimpleCursorAdapter) getListAdapter())
                .getItem(position);
        return (Phone) PoCHelper.getFromCursor(Phone.class, c);
	}

	@Override
	protected void callSyncPhoneEditWS(long phone) {

        PhoneRequestParams param = new PhoneRequestParams();
        param.userId = mUserId;
        param.phoneId = phone;

        mRequestHelper.startRequestForWorker(REQUEST_TYPE_EDIT, WorkerEditPhoneList.class,
                param);
	}

	@Override
	protected void callSyncPhoneAddWS(long phone) {
        PhoneRequestParams param = new PhoneRequestParams();
        param.userId = mUserId;
        param.phoneId = phone;
        
        mRequestHelper.startRequestForWorker(REQUEST_TYPE_ADD, WorkerAddPhoneList.class,
                param);
	}

	@Override
	protected void callSyncPhoneDeleteMonoWS() {
        Cursor c = (Cursor) ((SimpleCursorAdapter) getListAdapter())
                .getItem(mPositionToDelete);
        Phone phone = (Phone) PoCHelper.getFromCursor(Phone.class, c);

        PhoneRequestParams param = new PhoneRequestParams();
        param.userId = mUserId;
        param.phoneIds = String.valueOf(phone.id);
        
        mRequestHelper.startRequestForWorker(REQUEST_TYPE_DELETE_MONO, WorkerDeletePhoneList.class, param);
        
	}

	@Override
	protected void callSyncPhoneDeleteAllWS() {
        final StringBuilder sb = new StringBuilder();
        final SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        Cursor c = adapter.getCursor();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Phone phone = (Phone) PoCHelper.getFromCursor(Phone.class, c);

            sb.append(phone.id);
            if (c.moveToNext()) {
                sb.append(",");
            }
        }

        PhoneRequestParams param = new PhoneRequestParams();
        param.userId = mUserId;
        param.phoneIds = sb.toString();
        
        mRequestHelper.startRequestForWorker(REQUEST_TYPE_DELETE_ALL, WorkerDeletePhoneList.class, param);

	}
	
	@Override
	public void onRequestStarted(int requestType) {
		
		switch(requestType) {
		case REQUEST_TYPE_LIST:
			setProgressBarIndeterminateVisibility(true);
			break;
		case REQUEST_TYPE_DELETE_ALL:
		case REQUEST_TYPE_DELETE_MONO:
		case REQUEST_TYPE_EDIT:
		case REQUEST_TYPE_ADD:
			showDialog(DialogConfig.DIALOG_PROGRESS);
			break;
		}
	}

    @Override
    public void onRequestDataError(int requestType) {
        showDialog(DialogConfig.DIALOG_ERROR);
    }

    @Override
    public void onRequestConnectionError(int requestType, int statusCode) {
        showDialog(DialogConfig.DIALOG_CONNEXION_ERROR);
    }

    @Override
    public void onRequestCustomError(int requestType, Bundle resultData) {

    }

    @Override
    public void onRequestSuccessed(int requestType, Bundle payload) {
        if (requestType == REQUEST_TYPE_LIST) {
            mArePhonesLoaded = true;
        }
        updateAdapter();
    }

    @Override
	public void onRequestFinished(int requestType) {
		switch(requestType) {
		case REQUEST_TYPE_LIST:
			setProgressBarIndeterminateVisibility(false);
			break;
		case REQUEST_TYPE_DELETE_ALL:
		case REQUEST_TYPE_DELETE_MONO:
		case REQUEST_TYPE_EDIT:
		case REQUEST_TYPE_ADD:
			dismissDialog(DialogConfig.DIALOG_PROGRESS);
			break;
		}
	}
	
    private void updateAdapter() {
        Cursor c =  PoCHelper.init(ThisProvider.class).setContext(this).setClass(Phone.class).executeQuery();

        final SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        adapter.changeCursor(c);
        adapter.notifyDataSetChanged();
    }

}
