package com.foxykeep.datadroidpoc.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.foxykeep.datadroid.config.DialogConfig;
import com.foxykeep.datadroid.data.Phone;
import com.foxykeep.datadroidpoc.R;

public abstract class BaseListActivity extends ListActivity implements OnItemClickListener{

    protected static final int ACTIVITY_FOR_RESULT_ADD = 1;
    protected static final int ACTIVITY_FOR_RESULT_EDIT = 2;
    
    protected int mPositionToDelete;
    protected Phone mPhoneToAddUpdate;
	
    protected abstract Phone getPhoneOnPosition(int position);
    protected abstract void callSyncPhoneEditWS(Phone phone);
    protected abstract void callSyncPhoneAddWS(Phone phone);
    protected abstract void callSyncPhoneDeleteMonoWS();
    protected abstract void callSyncPhoneDeleteAllWS();
	
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crud_phone_list_context, menu);
        Phone p = getPhoneOnPosition(((AdapterContextMenuInfo) menuInfo).position);
        menu.setHeaderTitle(p.name);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();

        final int position = ((AdapterContextMenuInfo) item.getMenuInfo()).position;

        switch (itemId) {
        case R.id.menu_edit:
            Phone phone = getPhoneOnPosition(position);
            Intent intent = new Intent(this, CrudSyncPhoneAddEditActivity.class);
            intent.putExtra(CrudSyncPhoneAddEditActivity.INTENT_EXTRA_PHONE,
            		Phone.getPhone(phone));
            startActivityForResult(intent, ACTIVITY_FOR_RESULT_EDIT);
            return true;
        case R.id.menu_delete:
            mPositionToDelete = position;
            showDialog(DialogConfig.DIALOG_DELETE_CONFIRM);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }
    
    @Override
    public void onItemClick(final AdapterView<?> parent, View view,
            int position, long id) {
        if (parent == getListView()) {
            Intent intent = new Intent(this, CrudSyncPhoneViewActivity.class);
            Phone phone = getPhoneOnPosition(position);
            intent.putExtra(CrudSyncPhoneViewActivity.INTENT_EXTRA_PHONE, Phone.getPhone(phone));
            startActivity(intent);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crud_phone_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
        case R.id.menu_add:
            startActivityForResult(new Intent(this,
                    CrudSyncPhoneAddEditActivity.class),
                    ACTIVITY_FOR_RESULT_ADD);
            return true;
        case R.id.menu_delete_all:
            showDialog(DialogConfig.DIALOG_DELETE_ALL_CONFIRM);
            return true;
        case R.id.menu_copy_activity:
            startActivity(new Intent(this,
            		CopyOfNewMainActvity.class));
        default:
            return super.onContextItemSelected(item);
        }
    }
    
    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        switch (requestCode) {
        case ACTIVITY_FOR_RESULT_ADD: {
			if (data != null) {
				mPhoneToAddUpdate = Phone.getPhone(data
						.getStringExtra(CrudSyncPhoneAddEditActivity.INTENT_EXTRA_PHONE));
				callSyncPhoneAddWS(mPhoneToAddUpdate);
			}
            break;
        }
        case ACTIVITY_FOR_RESULT_EDIT: {
			if (data != null) {
				mPhoneToAddUpdate = Phone.getPhone(data
						.getStringExtra(CrudSyncPhoneAddEditActivity.INTENT_EXTRA_PHONE));
				callSyncPhoneEditWS(mPhoneToAddUpdate);
			}
            break;
        }
        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }
	
    
    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {
        switch (id) {
        case DialogConfig.DIALOG_DELETE_CONFIRM:
            Phone phone = getPhoneOnPosition(mPositionToDelete);
            ((AlertDialog) dialog).setMessage(getString(
                    R.string.crud_phone_list_dialog_delete_confirm_message,
                    phone.name));
            break;
        default:
            super.onPrepareDialog(id, dialog);
            break;
        }
    }
    
    @Override
    protected Dialog onCreateDialog(final int id) {
        Builder b;
        switch (id) {
        case DialogConfig.DIALOG_DELETE_ALL_CONFIRM:
            b = new Builder(this);
            b.setIcon(android.R.drawable.ic_dialog_alert);
            b.setTitle(R.string.crud_phone_list_dialog_delete_all_confirm_title);
            b.setMessage(R.string.crud_phone_list_dialog_delete_all_confirm_message);
            b.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int which) {
                            callSyncPhoneDeleteAllWS();
                        }
                    });
            b.setNegativeButton(android.R.string.cancel, null);
            b.setCancelable(true);
            return b.create();
        case DialogConfig.DIALOG_DELETE_CONFIRM:
            Phone phone = getPhoneOnPosition(mPositionToDelete);
            b = new Builder(this);
            b.setIcon(android.R.drawable.ic_dialog_alert);
            b.setTitle(R.string.crud_phone_list_dialog_delete_confirm_title);
            b.setMessage(getString(
                    R.string.crud_phone_list_dialog_delete_confirm_message,
                    phone.name));
            b.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int which) {
                            callSyncPhoneDeleteMonoWS();
                        }
                    });
            b.setNegativeButton(android.R.string.cancel, null);
            b.setCancelable(true);
            return b.create();
        default:
            return super.onCreateDialog(id);
        }
    }
    
    
}
