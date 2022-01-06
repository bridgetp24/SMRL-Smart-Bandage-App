package com.test.smartbandage.data_manager;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.installations.FirebaseInstallations;

public class User {



    /**
     * Phone serial number that the user is using
     */
    String id;

    public User() {
        String fullId = FirebaseInstallations.getInstance().getId().toString();
        this.id = fullId.substring(fullId.indexOf('@') + 1);
        Log.d("UserDebug", "User id set to " + this.id);
    }


    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @NonNull
    @Override
    public String toString() {
        return "id: " + id;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
