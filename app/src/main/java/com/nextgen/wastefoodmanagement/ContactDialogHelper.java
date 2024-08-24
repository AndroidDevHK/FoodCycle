package com.nextgen.wastefoodmanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class ContactDialogHelper {

    private Activity activity;
    private String phoneNumber;
    private String whatsapp;

    public ContactDialogHelper(Activity activity, String phoneNumber, String whatsapp) {
        this.activity = activity;
        this.phoneNumber = phoneNumber;
        this.whatsapp = whatsapp;
    }

    public void showContactDialog() {
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.contact_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        ImageView whatsappIcon = dialogView.findViewById(R.id.whatsapp_icon);
        ImageView callIcon = dialogView.findViewById(R.id.call_icon);
        ImageView messageIcon = dialogView.findViewById(R.id.message_icon);

        setWhatsappClickListener(whatsappIcon);
        setCallClickListener(callIcon);
        setMessageClickListener(messageIcon);
    }

    private void setWhatsappClickListener(ImageView whatsappIcon) {
        whatsappIcon.setOnClickListener(v -> openWhatsapp());
    }

    private void setCallClickListener(ImageView callIcon) {
        callIcon.setOnClickListener(v -> openDialer());
    }

    private void setMessageClickListener(ImageView messageIcon) {
        messageIcon.setOnClickListener(v -> openMessagingApp());
    }

    private void openWhatsapp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + whatsapp));
        activity.startActivity(intent);
    }

    private void openDialer() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        activity.startActivity(intent);
    }

    private void openMessagingApp() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        activity.startActivity(intent);
    }
}
