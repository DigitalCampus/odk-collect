/*
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * @author ctsims
 * @author carlhartung (modified for ODK)
 */
public class AndroidShortcuts extends Activity {

    Uri[] commands;
    String[] names;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        // The Android needs to know what shortcuts are available, generate the list
        if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            buildMenuList();
        }
    }


    private void buildMenuList() {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Uri> commands = new ArrayList<Uri>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select ODK Shortcut");

        
        Cursor c = getContentResolver().query(FormsColumns.CONTENT_URI, null, null, null, null);
        startManagingCursor(c);

        if (c.getCount() > 0) {
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                String formName = c.getString(c.getColumnIndex(FormsColumns.DISPLAY_NAME));
                names.add(formName);
                Uri uri = Uri.withAppendedPath(FormsColumns.CONTENT_URI, c.getString(c.getColumnIndex(FormsColumns._ID)));
                commands.add(uri);
            }
        }

        this.names = names.toArray(new String[0]);
        this.commands = commands.toArray(new Uri[0]);

        builder.setItems(this.names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                returnShortcut(AndroidShortcuts.this.names[item],
                    AndroidShortcuts.this.commands[item]);
            }
        });

        builder.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                AndroidShortcuts sc = AndroidShortcuts.this;
                sc.setResult(RESULT_CANCELED);
                sc.finish();
                return;
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    /**
     * 
     */
    private void returnShortcut(String name, Uri command) {
        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
        shortcutIntent.setData(command);

        // Home here makes the intent new every time you call it
        shortcutIntent.addCategory(Intent.CATEGORY_HOME);

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.notes);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        // Now, return the result to the launcher

        setResult(RESULT_OK, intent);
        finish();
        return;
    }
}
