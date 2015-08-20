package net.hitch_hiking.otostopproject;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;

/**
 * Created by user on 6.7.2015.
 */
public class SettingsActivity extends PreferenceActivity  implements OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final CheckBoxPreference showingPass = (CheckBoxPreference) getPreferenceScreen().findPreference("pass");
        showingPass.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                /*Log.d("MyApp", "Pref " + preference.getKey() + " changed to " + newValue.toString());
                return true;*/
                if (!showingPass.isChecked()){
                    String passwordFromFirebase = "password";
                    //passwordFromFirebase should initialize to database value
                    showingPass.setSummary(passwordFromFirebase);
                } else {
                    showingPass.setSummary("");
                }
                return true;
            }
        });

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause()
    {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume()
    {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        updatePrefSummary(findPreference(key));
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }
    private void changePassword(String pass){
        //set paswoord in firebase -> pass
        CheckBoxPreference forPass = (CheckBoxPreference) getPreferenceScreen().findPreference("pass");
        if(forPass.isChecked()){
            forPass.setSummary(pass);
        } else {
            forPass.setSummary("");
        }
    }

    private void updatePrefSummary(Preference p) {
        SharedPreferences.Editor editor = p.getEditor();
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (p.getTitle().toString().contains("ifre"))
            {
                String password = editTextPref.getText();
                changePassword(password);
            } else {
                p.setSummary(editTextPref.getText());
                //firebase should be updated for necessary preference.
                String title = p.getTitle().toString();
                switch (title){
                    case "NAME":
                        //set name in firebase
                        break;
                    case "SURNAME":
                        //set surname in firebase
                        break;
                    case "E-MAIL":
                        //set e-mail in firebase
                        break;
                    case "BIRTHDAY":
                        //set birthday in firebase
                        break;
                    case "PHONE NUMBER":
                        //set phone number in firebase
                        break;
                    case "CAR INFO":
                        //set car info in firebase
                        break;

                }
                editor.commit();
                editor.apply();

            }
        }
    }
}