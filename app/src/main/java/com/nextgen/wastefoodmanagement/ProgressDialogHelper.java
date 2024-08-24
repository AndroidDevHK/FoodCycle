package com.nextgen.wastefoodmanagement;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogHelper {

    private static ProgressDialog progressDialog;

    public static void showProgressDialog(Context context, String message) {
        dismissProgressDialog(); // Dismiss any existing dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false); // Prevent user from canceling dialog
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
