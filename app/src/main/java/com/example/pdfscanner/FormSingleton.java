package com.example.pdfscanner;

import android.content.Context;

public class FormSingleton {
    private static FormSingleton formSingleton;
    private Form form;

    public FormSingleton(Context context) {
        form = new Form();
    }

    public static FormSingleton get(Context context) {
        if (formSingleton == null) {
            formSingleton = new FormSingleton(context);
        }
        return formSingleton;
    }

    public Form getForm() {
        return form;
    }

    public static FormSingleton newDataSingleton(Context context) {
        formSingleton = new FormSingleton(context);
        return formSingleton;
    }

    public void Recycle() {
        if (form!=null) {
            form.Recycle();
        }
        if (formSingleton!=null) {
            formSingleton = null;
        }
    }
}
