package com.example.readcontact;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.security.Permission;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    ListView lvContact;
    ProgressBar progressBar;

    private ContactAdapter mAdapter;

    private final int Contact_request_code = 101;
    private final int Contact_load_code = 104;
    private final int Contact_select_code = 105;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvContact = findViewById(R.id.lv_contact);
        progressBar = findViewById(R.id.progress_bar);

        lvContact.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.GONE);

        mAdapter = new ContactAdapter(MainActivity.this, null, true);
        lvContact.setAdapter(mAdapter);
        lvContact.setOnItemClickListener(this);

        if (isGranted(Manifest.permission.READ_CONTACTS)){
            getSupportLoaderManager().initLoader(Contact_load_code, null, this);
        }else{
            checkPermission(Manifest.permission.READ_CONTACTS, Contact_request_code);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
        long contactId = cursor.getLong(0);
        Log.d("CCC", "onItemClick: "+position);

        Bundle bundle = new Bundle();
        bundle.putString("id", String.valueOf(contactId));
        getSupportLoaderManager().restartLoader(Contact_select_code, bundle, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader mCursorLoader = null;
        if (id == Contact_load_code){
            progressBar.setVisibility(View.VISIBLE);

            String[] projectionFields = new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
            };

            mCursorLoader = new CursorLoader(MainActivity.this,
                    ContactsContract.Contacts.CONTENT_URI, projectionFields,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1", null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        }else if(id == Contact_select_code){
            String[] phoneProjectionFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };

            mCursorLoader = new CursorLoader(MainActivity.this,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    phoneProjectionFields,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND "+
                            ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE + " AND " +
                            ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1",
                    new String[]{args.getString("id")},
                    null);


        }
        return mCursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == Contact_load_code){
            if (data.getCount() > 0){
                lvContact.setVisibility(View.VISIBLE);
                mAdapter.swapCursor(data);
            }
            progressBar.setVisibility(View.GONE);
        } else if(loader.getId() == Contact_select_code){
            String contactNumber = null;
            String name = null;
            if (data.moveToFirst()){
                name = data.getString(data.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                contactNumber = data.getString(data.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            Toast.makeText(this, name + contactNumber, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == Contact_load_code){
            progressBar.setVisibility(View.GONE);
            mAdapter.swapCursor(null);
            Log.d("YYY", "onLoaderReset: ");
        }
    }

    private void checkPermission(String permission, int requestCode){
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    private boolean isGranted(String permission){
        return (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getSupportLoaderManager().initLoader(Contact_load_code, null,  this);
                Toast.makeText(this, "Contact permission diterima", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Contact permission ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
